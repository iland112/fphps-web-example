package com.smartcoreinc.fphps.example.fphps_web_example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

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
     *
     * @return PA API 통신용 RestTemplate
     */
    @Bean
    public RestTemplate paApiRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setUriTemplateHandler(
            new DefaultUriBuilderFactory(baseUrl)
        );
        return restTemplate;
    }
}
