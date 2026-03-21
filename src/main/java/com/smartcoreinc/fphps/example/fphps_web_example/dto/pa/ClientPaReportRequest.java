package com.smartcoreinc.fphps.example.fphps_web_example.dto.pa;

/**
 * 클라이언트 PA Step 2: 검증 결과 보고 요청 DTO
 * POST /api/pa/trust-materials/result
 */
public record ClientPaReportRequest(
    String requestId,
    String verificationStatus,
    String verificationMessage,
    Boolean trustChainValid,
    Boolean sodSignatureValid,
    Boolean dgHashValid,
    Boolean crlCheckPassed,
    Integer processingTimeMs,
    String encryptedMrz
) {}
