package com.smartcoreinc.fphps.example.fphps_web_example.dto.pa;

/**
 * SOD 서명 검증 결과
 */
public record SodSignatureValidation(
    boolean valid,
    String signatureAlgorithm,
    String hashAlgorithm,
    String validationErrors
) {}
