package com.smartcoreinc.fphps.example.fphps_web_example.dto.pa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * SOD 서명 검증 결과
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SodSignatureValidation(
    boolean valid,
    String signatureAlgorithm,
    String hashAlgorithm,
    String validationErrors
) {}
