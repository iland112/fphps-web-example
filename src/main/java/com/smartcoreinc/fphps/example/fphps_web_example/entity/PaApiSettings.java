package com.smartcoreinc.fphps.example.fphps_web_example.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * PA API 설정을 영구 저장하기 위한 JPA 엔티티
 * SQLite 데이터베이스에 PA API Base URL과 API Key를 저장
 */
@Entity
@Table(name = "pa_api_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaApiSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "base_url")
    private String baseUrl;

    @Column(name = "api_key")
    private String apiKey;
}
