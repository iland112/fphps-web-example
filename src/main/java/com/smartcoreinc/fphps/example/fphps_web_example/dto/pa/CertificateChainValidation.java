package com.smartcoreinc.fphps.example.fphps_web_example.dto.pa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 인증서 체인 검증 결과
 *
 * API Gateway 응답 필드:
 * - valid: 전체 인증서 체인 유효 여부
 * - dscSubject/dscIssuer/dscSerialNumber: DSC 인증서 정보
 * - cscaSubject/cscaSerialNumber: CSCA 인증서 정보
 * - trustChainPath/trustChainDepth: 인증서 체인 경로 정보
 *
 * CRL 검증 필드:
 * - crlStatus: CRL 상태 코드 (예: CRL_VALID, CRL_UNAVAILABLE, CRL_REVOKED, CRL_EXPIRED)
 * - crlStatusDescription: 상태 설명
 * - crlStatusDetailedDescription: 상세 설명
 * - crlStatusSeverity: 심각도 (SUCCESS, WARNING, ERROR, INFO)
 * - crlMessage: 기술적 메시지
 * - crlThisUpdate: CRL 발행일 (ISO 8601)
 * - crlNextUpdate: CRL 다음 업데이트 예정일 (ISO 8601)
 *
 * Certificate Expiration 필드:
 * - dscExpired: DSC 인증서 만료 여부
 * - cscaExpired: CSCA 인증서 만료 여부
 * - validAtSigningTime: 여권 서명 당시 인증서 유효 여부 (Point-in-Time Validation)
 * - expirationStatus: 만료 상태 (VALID, WARNING, EXPIRED)
 * - expirationMessage: 만료 상태 설명 메시지
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CertificateChainValidation(
    boolean valid,
    String validationStatus,
    String dscSubject,
    String dscIssuer,
    String dscSerialNumber,
    String cscaSubject,
    String cscaSerialNumber,
    String trustChainPath,
    Integer trustChainDepth,
    Boolean signatureVerified,
    Boolean fullyValid,
    boolean crlChecked,
    boolean revoked,
    String crlStatus,
    String crlStatusDescription,
    String crlStatusDetailedDescription,
    String crlStatusSeverity,
    String crlMessage,
    String crlThisUpdate,
    String crlNextUpdate,
    String validationErrors,

    // Certificate Expiration fields
    Boolean dscExpired,
    Boolean cscaExpired,
    Boolean validAtSigningTime,
    String expirationStatus,
    String expirationMessage
) {}
