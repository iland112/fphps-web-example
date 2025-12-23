package com.smartcoreinc.fphps.example.fphps_web_example.dto.pa;

import java.time.LocalDateTime;

/**
 * 인증서 체인 검증 결과
 */
public record CertificateChainValidation(
    boolean valid,
    String dscSubject,
    String dscSerialNumber,
    String cscaSubject,
    String cscaSerialNumber,
    LocalDateTime notBefore,
    LocalDateTime notAfter,
    boolean crlChecked,
    boolean revoked,
    String crlStatus,
    String crlMessage,
    String validationErrors
) {}
