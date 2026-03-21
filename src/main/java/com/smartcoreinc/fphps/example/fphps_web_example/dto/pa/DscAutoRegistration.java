package com.smartcoreinc.fphps.example.fphps_web_example.dto.pa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DSC 자동 등록 결과 (v2.1.0+)
 * PA 검증 시 SOD에서 추출된 DSC를 Local PKD에 자동 등록한 결과
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DscAutoRegistration(
    boolean registered,
    boolean newlyRegistered,
    String certificateId,
    String fingerprint,
    String countryCode
) {}
