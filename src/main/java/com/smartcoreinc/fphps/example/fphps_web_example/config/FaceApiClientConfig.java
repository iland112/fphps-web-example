package com.smartcoreinc.fphps.example.fphps_web_example.config;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * Face API 클라이언트 설정
 * InsightFace 기반 Face Verification API와 통신하기 위한 RestTemplate 설정
 * Apache HttpClient 5 사용으로 안정적인 HTTP 연결 제공
 */
@Slf4j
@Configuration
public class FaceApiClientConfig {

    @Value("${face-api.base-url:http://localhost:8082}")
    private String baseUrl;

    /**
     * Face API 전용 RestTemplate 빈 생성
     * Apache HttpClient 5를 사용하여 안정적인 연결 관리
     * - Connection Pool 사용으로 연결 재사용
     * - 타임아웃 설정: 연결 30초, 소켓 60초, 응답 60초
     *
     * @return Face API 통신용 RestTemplate
     */
    @Bean
    public RestTemplate faceApiRestTemplate() {
        log.info("Initializing Face API RestTemplate with base URL: {}", baseUrl);

        // Connection Manager 설정 - 연결 풀 및 소켓 타임아웃
        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
            .setDefaultConnectionConfig(
                ConnectionConfig.custom()
                    .setSocketTimeout(Timeout.ofSeconds(60))
                    .setConnectTimeout(Timeout.ofSeconds(30))
                    .build()
            )
            .setMaxConnTotal(10)
            .setMaxConnPerRoute(5)
            .build();

        // Request 설정 - 응답 타임아웃
        RequestConfig requestConfig = RequestConfig.custom()
            .setResponseTimeout(Timeout.ofSeconds(60))
            .setConnectionRequestTimeout(Timeout.ofSeconds(30))
            .build();

        // HttpClient 생성
        CloseableHttpClient httpClient = HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .build();

        // HttpComponentsClientHttpRequestFactory 사용
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(baseUrl));

        log.info("Face API RestTemplate initialized successfully with Apache HttpClient 5");
        return restTemplate;
    }
}
