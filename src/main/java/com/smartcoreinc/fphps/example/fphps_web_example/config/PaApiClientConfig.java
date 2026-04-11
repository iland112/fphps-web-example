package com.smartcoreinc.fphps.example.fphps_web_example.config;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import lombok.extern.slf4j.Slf4j;

/**
 * PA API 클라이언트 설정
 * - Apache HttpClient 5 기반 RestTemplate
 * - 커스텀 CA 인증서 지원 (data/certs/pa-ca.crt)
 * - 런타임 SSL 컨텍스트 재구성 지원
 */
@Slf4j
@Configuration
public class PaApiClientConfig {

    public static final String CA_CERT_DIR = "data/certs";
    public static final String CA_CERT_FILE = "pa-ca.crt";

    @Value("${pa-api.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * PA API RestTemplate Bean 생성.
     * API Key 인터셉터는 여기서 설정하지 않음 - PaApiSettingsService가
     * SQLite DB에서 로드한 키로 런타임에 설정함.
     */
    @Bean
    public RestTemplate paApiRestTemplate() {
        log.info("Initializing PA API RestTemplate with base URL: {}", baseUrl);

        RestTemplate restTemplate = new RestTemplate(createHttpRequestFactory());
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(baseUrl));

        log.info("PA API RestTemplate initialized (API Key will be configured by PaApiSettingsService from DB)");
        return restTemplate;
    }

    /**
     * HttpComponentsClientHttpRequestFactory 생성
     * 커스텀 CA 인증서가 있으면 해당 인증서를 포함한 SSL 컨텍스트 사용
     */
    public static HttpComponentsClientHttpRequestFactory createHttpRequestFactory() {
        return new HttpComponentsClientHttpRequestFactory(createHttpClient());
    }

    /**
     * Apache HttpClient 5 생성
     * data/certs/pa-ca.crt 파일이 있으면 커스텀 TrustManager 적용
     */
    public static CloseableHttpClient createHttpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
            .setResponseTimeout(Timeout.ofSeconds(60))
            .setConnectionRequestTimeout(Timeout.ofSeconds(30))
            .build();

        ConnectionConfig connectionConfig = ConnectionConfig.custom()
            .setSocketTimeout(Timeout.ofSeconds(60))
            .setConnectTimeout(Timeout.ofSeconds(30))
            .build();

        Path caCertPath = Path.of(CA_CERT_DIR, CA_CERT_FILE);

        PoolingHttpClientConnectionManager connectionManager;

        if (Files.exists(caCertPath)) {
            try {
                SSLContext sslContext = createSslContext(caCertPath);
                connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                    .setSSLSocketFactory(
                        SSLConnectionSocketFactoryBuilder.create()
                            .setSslContext(sslContext)
                            .build()
                    )
                    .setDefaultConnectionConfig(connectionConfig)
                    .setMaxConnTotal(10)
                    .setMaxConnPerRoute(5)
                    .build();
                log.info("Custom CA certificate loaded: {}", caCertPath);
            } catch (Exception e) {
                log.warn("Failed to load custom CA certificate, using default: {}", e.getMessage());
                connectionManager = createDefaultConnectionManager(connectionConfig);
            }
        } else {
            log.debug("No custom CA certificate found at {}, using default truststore", caCertPath);
            connectionManager = createDefaultConnectionManager(connectionConfig);
        }

        return HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .build();
    }

    /**
     * 커스텀 CA 인증서를 포함한 SSLContext 생성
     * 시스템 기본 truststore + 커스텀 CA 인증서를 합친 복합 TrustManager
     */
    public static SSLContext createSslContext(Path caCertPath) throws Exception {
        // 커스텀 CA 인증서 로드
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate caCert;
        try (InputStream is = Files.newInputStream(caCertPath)) {
            caCert = (X509Certificate) cf.generateCertificate(is);
        }
        log.info("Custom CA: subject={}, notAfter={}",
            caCert.getSubjectX500Principal().getName(), caCert.getNotAfter());

        // 커스텀 CA를 포함한 KeyStore 생성
        KeyStore customTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        customTrustStore.load(null, null);
        customTrustStore.setCertificateEntry("pa-custom-ca", caCert);

        // 시스템 기본 TrustManager
        TrustManagerFactory defaultTmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        defaultTmf.init((KeyStore) null);
        X509TrustManager defaultTm = findX509TrustManager(defaultTmf);

        // 커스텀 CA TrustManager
        TrustManagerFactory customTmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        customTmf.init(customTrustStore);
        X509TrustManager customTm = findX509TrustManager(customTmf);

        // 복합 TrustManager: 시스템 기본 → 커스텀 CA 순서로 검증
        X509TrustManager compositeTm = new CompositeX509TrustManager(defaultTm, customTm);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{compositeTm}, null);
        return sslContext;
    }

    private static X509TrustManager findX509TrustManager(TrustManagerFactory tmf) {
        for (TrustManager tm : tmf.getTrustManagers()) {
            if (tm instanceof X509TrustManager) {
                return (X509TrustManager) tm;
            }
        }
        throw new IllegalStateException("No X509TrustManager found");
    }

    private static PoolingHttpClientConnectionManager createDefaultConnectionManager(ConnectionConfig connectionConfig) {
        return PoolingHttpClientConnectionManagerBuilder.create()
            .setDefaultConnectionConfig(connectionConfig)
            .setMaxConnTotal(10)
            .setMaxConnPerRoute(5)
            .build();
    }

    /**
     * 복합 X509TrustManager
     * 기본 truststore에서 먼저 검증, 실패하면 커스텀 CA로 재시도
     */
    private static class CompositeX509TrustManager implements X509TrustManager {
        private final X509TrustManager defaultTm;
        private final X509TrustManager customTm;

        CompositeX509TrustManager(X509TrustManager defaultTm, X509TrustManager customTm) {
            this.defaultTm = defaultTm;
            this.customTm = customTm;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws java.security.cert.CertificateException {
            defaultTm.checkClientTrusted(chain, authType);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws java.security.cert.CertificateException {
            try {
                defaultTm.checkServerTrusted(chain, authType);
            } catch (java.security.cert.CertificateException e) {
                // 기본 truststore에서 실패하면 커스텀 CA로 재시도
                customTm.checkServerTrusted(chain, authType);
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            X509Certificate[] defaultIssuers = defaultTm.getAcceptedIssuers();
            X509Certificate[] customIssuers = customTm.getAcceptedIssuers();
            X509Certificate[] combined = new X509Certificate[defaultIssuers.length + customIssuers.length];
            System.arraycopy(defaultIssuers, 0, combined, 0, defaultIssuers.length);
            System.arraycopy(customIssuers, 0, combined, defaultIssuers.length, customIssuers.length);
            return combined;
        }
    }
}
