package com.smartcoreinc.fphps.example.fphps_web_example.dto.pa;

import java.time.LocalDateTime;

/**
 * 인증서 체인 검증 결과
 *
 * CRL 검증 API 응답 필드:
 * - crlStatus: CRL 상태 코드 (예: CRL_VALID, CRL_UNAVAILABLE, CRL_REVOKED)
 * - crlStatusDescription: 상태 설명 (예: "CRL not available in LDAP")
 * - crlStatusDetailedDescription: 상세 설명 (예: "LDAP 서버에서 CRL을 조회할 수 없습니다...")
 * - crlStatusSeverity: 심각도 (SUCCESS, WARNING, ERROR, INFO)
 * - crlMessage: 기술적 메시지 (예: "LDAP에서 해당 CSCA의 CRL을 찾을 수 없음...")
 *
 * v1.2.0 Certificate Expiration 필드:
 * - dscExpired: DSC 인증서 만료 여부
 * - cscaExpired: CSCA 인증서 만료 여부
 * - validAtSigningTime: 여권 서명 당시 인증서 유효 여부 (Point-in-Time Validation)
 * - expirationStatus: 만료 상태 (VALID, WARNING, EXPIRED)
 * - expirationMessage: 만료 상태 설명 메시지
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
    String crlStatusDescription,
    String crlStatusDetailedDescription,
    String crlStatusSeverity,
    String crlMessage,
    String validationErrors,

    // v1.2.0 Certificate Expiration fields
    Boolean dscExpired,
    Boolean cscaExpired,
    Boolean validAtSigningTime,
    String expirationStatus,
    String expirationMessage
) {}
