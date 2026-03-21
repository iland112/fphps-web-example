package com.smartcoreinc.fphps.example.fphps_web_example.dto.pa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * PA Lookup 검증 결과 DTO
 * DSC Trust Chain 검증 상세 정보
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PaLookupValidation(
    String id,
    String certificateType,
    String countryCode,
    String subjectDn,
    String issuerDn,
    String serialNumber,
    String validationStatus,        // VALID, EXPIRED_VALID, INVALID, PENDING, ERROR
    boolean trustChainValid,
    String trustChainMessage,
    String trustChainPath,
    boolean cscaFound,
    String cscaSubjectDn,
    boolean signatureValid,
    String signatureAlgorithm,
    boolean validityPeriodValid,
    String notBefore,
    String notAfter,
    String revocationStatus,        // not_revoked, revoked, unknown
    boolean crlChecked,
    String fingerprintSha256,
    String validatedAt,

    // DSC Non-Conformant fields (v2.1.4+)
    String pkdConformanceCode,
    String pkdConformanceText,
    String pkdVersion
) {}
