package com.smartcoreinc.fphps.example.fphps_web_example.Services;

import com.smartcoreinc.fphps.example.fphps_web_example.config.PaApiClientConfig;
import com.smartcoreinc.fphps.example.fphps_web_example.entity.PaApiSettings;
import com.smartcoreinc.fphps.example.fphps_web_example.repository.PaApiSettingsRepository;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * PA API 설정 관리 서비스
 * - 런타임에 PA API Base URL 및 API Key 변경 가능
 * - CA 인증서 업로드 및 SSL 재설정
 * - SQLite에 설정 영구 저장
 * - RestTemplate을 동적으로 재설정
 */
@Slf4j
@Service
public class PaApiSettingsService {

    private final PaApiSettingsRepository repository;
    private final RestTemplate paApiRestTemplate;

    @Value("${pa-api.base-url:http://localhost:8080}")
    private String defaultBaseUrl;

    @Value("${pa-api.api-key:}")
    private String defaultApiKey;

    private String baseUrl;
    private String apiKey;
    private Long currentSettingsId;

    public PaApiSettingsService(PaApiSettingsRepository repository,
                                @Qualifier("paApiRestTemplate") RestTemplate paApiRestTemplate) {
        this.repository = repository;
        this.paApiRestTemplate = paApiRestTemplate;
    }

    @PostConstruct
    public void init() {
        Optional<PaApiSettings> saved = repository.findTopByOrderByIdDesc();
        if (saved.isPresent()) {
            PaApiSettings settings = saved.get();
            this.baseUrl = settings.getBaseUrl();
            this.apiKey = settings.getApiKey();
            this.currentSettingsId = settings.getId();
            log.info("PA API settings loaded from database: baseUrl={}", baseUrl);
            reconfigureRestTemplate();
        } else {
            this.baseUrl = defaultBaseUrl;
            this.apiKey = defaultApiKey;
            log.info("PA API settings using defaults from application.properties: baseUrl={}", baseUrl);
        }
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    /**
     * PA API 설정 업데이트 및 RestTemplate 재설정
     */
    public void updateSettings(String newBaseUrl, String newApiKey) {
        this.baseUrl = newBaseUrl != null ? newBaseUrl.trim() : defaultBaseUrl;
        this.apiKey = newApiKey != null ? newApiKey.trim() : "";

        PaApiSettings entity = PaApiSettings.builder()
                .baseUrl(this.baseUrl)
                .apiKey(this.apiKey)
                .build();

        if (currentSettingsId != null) {
            entity.setId(currentSettingsId);
        }

        PaApiSettings saved = repository.save(entity);
        this.currentSettingsId = saved.getId();

        reconfigureRestTemplate();
        log.info("PA API settings updated: baseUrl={}", this.baseUrl);
    }

    /**
     * CA 인증서 업로드 및 SSL 재설정
     *
     * @param certFile 업로드된 인증서 파일 (.crt, .pem)
     * @return 인증서 정보 문자열
     */
    public String uploadCaCertificate(MultipartFile certFile) throws Exception {
        if (certFile == null || certFile.isEmpty()) {
            throw new IllegalArgumentException("Certificate file is empty");
        }

        // 인증서 유효성 검증
        X509Certificate cert;
        try (InputStream is = certFile.getInputStream()) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate) cf.generateCertificate(is);
        }

        // 기본 유효성 확인
        cert.checkValidity();

        // data/certs/ 디렉토리 생성
        Path certDir = Path.of(PaApiClientConfig.CA_CERT_DIR);
        Files.createDirectories(certDir);

        // 파일 저장
        Path certPath = certDir.resolve(PaApiClientConfig.CA_CERT_FILE);
        try (InputStream is = certFile.getInputStream()) {
            Files.copy(is, certPath, StandardCopyOption.REPLACE_EXISTING);
        }

        log.info("CA certificate saved: subject={}, issuer={}, notAfter={}",
            cert.getSubjectX500Principal().getName(),
            cert.getIssuerX500Principal().getName(),
            cert.getNotAfter());

        // SSL 재설정 (RestTemplate의 HttpClient를 새로 생성)
        rebuildHttpClient();

        String info = String.format("Subject: %s, Issuer: %s, Valid until: %s",
            cert.getSubjectX500Principal().getName(),
            cert.getIssuerX500Principal().getName(),
            cert.getNotAfter());

        return info;
    }

    /**
     * 현재 저장된 CA 인증서 정보 조회
     *
     * @return 인증서 정보 또는 null
     */
    public String getCaCertificateInfo() {
        Path certPath = Path.of(PaApiClientConfig.CA_CERT_DIR, PaApiClientConfig.CA_CERT_FILE);
        if (!Files.exists(certPath)) {
            return null;
        }

        try (InputStream is = Files.newInputStream(certPath)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(is);

            return String.format("Subject: %s | Valid: %s ~ %s",
                cert.getSubjectX500Principal().getName(),
                cert.getNotBefore(),
                cert.getNotAfter());
        } catch (Exception e) {
            log.warn("Failed to read CA certificate info: {}", e.getMessage());
            return "Error reading certificate: " + e.getMessage();
        }
    }

    /**
     * CA 인증서 삭제
     */
    public void deleteCaCertificate() throws Exception {
        Path certPath = Path.of(PaApiClientConfig.CA_CERT_DIR, PaApiClientConfig.CA_CERT_FILE);
        if (Files.exists(certPath)) {
            Files.delete(certPath);
            log.info("CA certificate deleted: {}", certPath);
            rebuildHttpClient();
        }
    }

    /**
     * RestTemplate의 Base URL과 API Key 인터셉터를 런타임에 재설정
     */
    private void reconfigureRestTemplate() {
        paApiRestTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(this.baseUrl));

        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        if (this.apiKey != null && !this.apiKey.isBlank()) {
            interceptors.add((request, body, execution) -> {
                request.getHeaders().set("X-API-Key", this.apiKey);
                return execution.execute(request, body);
            });
        }

        paApiRestTemplate.setInterceptors(interceptors);
        log.debug("RestTemplate reconfigured: baseUrl={}, apiKey={}",
                  this.baseUrl,
                  this.apiKey != null && !this.apiKey.isBlank() ? "configured" : "empty");
    }

    /**
     * HttpClient를 새로 생성하여 RestTemplate에 적용
     * CA 인증서 변경 시 SSL 컨텍스트를 재구성
     */
    private void rebuildHttpClient() {
        try {
            paApiRestTemplate.setRequestFactory(PaApiClientConfig.createHttpRequestFactory());
            log.info("RestTemplate HttpClient rebuilt with updated SSL context");
        } catch (Exception e) {
            log.error("Failed to rebuild HttpClient: {}", e.getMessage(), e);
        }
    }
}
