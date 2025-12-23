package com.smartcoreinc.fphps.example.fphps_web_example.dto.pa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * PA 검증 응답 DTO
 * Local PKD API의 POST /api/pa/verify 엔드포인트로부터 받는 응답 데이터
 */
public record PaVerificationResponse(
    String status,
    UUID verificationId,
    LocalDateTime verificationTimestamp,
    String issuingCountry,
    String documentNumber,
    CertificateChainValidation certificateChainValidation,
    SodSignatureValidation sodSignatureValidation,
    DataGroupValidation dataGroupValidation,
    Long processingDurationMs,
    List<PaError> errors
) {
    /**
     * 검증 결과가 유효한지 확인
     *
     * @return status가 "VALID"이면 true
     */
    public boolean isValid() {
        return "VALID".equals(status);
    }

    /**
     * 검증 결과가 무효한지 확인
     *
     * @return status가 "INVALID"이면 true
     */
    public boolean isInvalid() {
        return "INVALID".equals(status);
    }

    /**
     * 검증 중 에러가 발생했는지 확인
     *
     * @return status가 "ERROR"이면 true
     */
    public boolean hasError() {
        return "ERROR".equals(status);
    }
}
