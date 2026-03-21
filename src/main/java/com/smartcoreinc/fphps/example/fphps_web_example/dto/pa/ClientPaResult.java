package com.smartcoreinc.fphps.example.fphps_web_example.dto.pa;

import java.util.List;
import java.util.Map;

/**
 * 클라이언트 모드 PA 검증 결과 DTO
 *
 * 로컬에서 SOD 서명 검증 + DG 해시 검증을 수행하고,
 * Trust Chain만 PA Lookup API로 조회하여 종합한 결과
 */
public record ClientPaResult(
    String verificationMode,           // "CLIENT"
    String overallStatus,              // VALID, INVALID, PARTIAL (Trust Chain 미확인 시)
    long processingDurationMs,         // 로컬 처리 시간 (ms)

    // 로컬 SOD 서명 검증 결과
    SodSignatureResult sodSignature,

    // 로컬 DG 해시 검증 결과
    DgHashResult dgHashValidation,

    // 원격 Trust Chain 조회 결과 (PA Lookup)
    PaLookupValidation trustChainLookup,   // null if PA API unavailable
    boolean trustChainAvailable,            // PA API 연결 성공 여부

    // DSC 인증서 정보 (로컬 추출)
    DscInfo dscInfo,

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
        Map<String, DgHashDetail> details
    ) {}

    /**
     * 개별 DG 해시 검증 상세
     */
    public record DgHashDetail(
        boolean valid,
        String expectedHash,
        String actualHash
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
