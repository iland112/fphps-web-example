package com.smartcoreinc.fphps.example.fphps_web_example.Services;

import com.smartcoreinc.fphps.example.fphps_web_example.entity.PaApiSettings;
import com.smartcoreinc.fphps.example.fphps_web_example.repository.PaApiSettingsRepository;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * PA API 설정 관리 서비스
 * - 런타임에 PA API Base URL 및 API Key 변경 가능
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

        // SQLite에 저장
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
     * RestTemplate의 Base URL과 API Key 인터셉터를 런타임에 재설정
     */
    private void reconfigureRestTemplate() {
        // Base URL 변경
        paApiRestTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(this.baseUrl));

        // 기존 인터셉터에서 API Key 인터셉터를 제거하고 새로 추가
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        for (ClientHttpRequestInterceptor interceptor : paApiRestTemplate.getInterceptors()) {
            // 기존 API Key 인터셉터 제외 (새로 추가할 것이므로)
            // ClientHttpRequestInterceptor는 익명 클래스이므로 모두 제거 후 재추가
        }

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
}
