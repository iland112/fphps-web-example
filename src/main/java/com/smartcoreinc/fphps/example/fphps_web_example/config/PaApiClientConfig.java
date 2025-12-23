package com.smartcoreinc.fphps.example.fphps_web_example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.time.Duration;

/**
 * PA API 클라이언트 설정
 * Local PKD의 Passive Authentication API와 통신하기 위한 RestTemplate 설정
 */
@Configuration
public class PaApiClientConfig {

    @Value("${pa-api.base-url:http://localhost:8081}")
    private String baseUrl;

    /**
     * PA API 전용 RestTemplate 빈 생성
     * baseUrl이 자동으로 모든 요청에 적용됨
     * 타임아웃 설정: 연결 30초, 읽기 60초 (PA 검증은 시간이 걸릴 수 있음)
     *
     * @return PA API 통신용 RestTemplate
     */
    @Bean
    public RestTemplate paApiRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(30).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(60).toMillis());

        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.setUriTemplateHandler(
            new DefaultUriBuilderFactory(baseUrl)
        );
        return restTemplate;
    }
}
