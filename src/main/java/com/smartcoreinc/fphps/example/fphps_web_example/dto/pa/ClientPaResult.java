package com.smartcoreinc.fphps.example.fphps_web_example.dto.pa;

import java.util.List;
import java.util.Map;

/**
 * 클라이언트 모드 PA 검증 결과 DTO
 *
 * 1. 서버에서 Trust Materials(CSCA/CRL/LC) 다운로드
 * 2. 로컬에서 SOD 서명 검증 + DG 해시 검증 + Trust Chain 검증 + CRL 체크
 * 3. 검증 결과를 서버에 보고
 */
public record ClientPaResult(
    String verificationMode,           // "CLIENT"
    String overallStatus,              // VALID, INVALID, PARTIAL, ERROR
    long processingDurationMs,

    // 서버 발급 requestId (결과 보고에 사용)
    String requestId,

    // 로컬 SOD 서명 검증 결과
    SodSignatureResult sodSignature,

    // 로컬 DG 해시 검증 결과
    DgHashResult dgHashValidation,

    // 로컬 Trust Chain 검증 결과 (Trust Materials 기반)
    TrustChainResult trustChainResult,

    // 로컬 CRL 체크 결과
    CrlCheckResult crlCheckResult,

    // Trust Materials 다운로드 정보
    TrustMaterialsInfo trustMaterialsInfo,

    // DSC 인증서 정보 (로컬 추출)
    DscInfo dscInfo,

    // 서버 보고 상태
    boolean serverReported,
    String serverReportError,

    // 에러 목록
    List<String> errors
) {

    /**
     * 로컬 SOD 서명 검증 결과
     */
    public record SodSignatureResult(
        boolean valid,
        String hashAlgorithm,
        String signatureAlgorithm,
        String errorMessage
    ) {}

    /**
     * 로컬 DG 해시 검증 결과
     */
    public record DgHashResult(
        int totalGroups,
        int validGroups,
        int invalidGroups,
        int skippedGroups,
        Map<String, DgHashDetail> details
    ) {}

    /**
     * 개별 DG 해시 검증 상세
     */
    public record DgHashDetail(
        boolean valid,
        boolean skipped,
        String expectedHash,
        String actualHash
    ) {}

    /**
     * 로컬 Trust Chain 검증 결과
     */
    public record TrustChainResult(
        boolean available,         // Trust Materials 다운로드 성공 여부
        boolean valid,             // DSC → CSCA Trust Chain 유효 여부
        boolean cscaFound,
        String cscaSubject,
        String chainPath,          // "DSC → CSCA" 또는 "DSC → Link → CSCA"
        String errorMessage
    ) {}

    /**
     * 로컬 CRL 체크 결과
     */
    public record CrlCheckResult(
        boolean checked,           // CRL 체크 수행 여부
        boolean passed,            // CRL 체크 통과 여부 (미폐기)
        boolean revoked,
        String crlIssuer,
        String errorMessage
    ) {}

    /**
     * Trust Materials 다운로드 정보
     */
    public record TrustMaterialsInfo(
        int cscaCount,
        int linkCertCount,
        int crlCount,
        String countryCode
    ) {}

    /**
     * 로컬 추출 DSC 인증서 정보
     */
    public record DscInfo(
        String subject,
        String issuer,
        String serialNumber,
        String notBefore,
        String notAfter,
        boolean expired,
        String signatureAlgorithm,
        String publicKeyAlgorithm,
        int publicKeySize,
        String sha256Fingerprint
    ) {}
}
