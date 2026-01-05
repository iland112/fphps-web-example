package com.smartcoreinc.fphps.example.fphps_web_example.dto.pa;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * PA V2 API 실제 검증 데이터 DTO
 * API Gateway 응답의 "data" 객체 내용
 */
public record PaVerificationData(
    String status,
    String verificationId,
    String verificationTimestamp,
    String issuingCountry,
    String documentNumber,
    int processingDurationMs,
    CertificateChainValidation certificateChainValidation,
    SodSignatureValidation sodSignatureValidation,
    DataGroupValidation dataGroupValidation,
    List<PaError> errors
) {
    /**
     * 기존 PaVerificationResponse 형식으로 변환
     * V1 API 응답과 호환성 유지를 위해 사용
     *
     * @return PaVerificationResponse
     */
    public PaVerificationResponse toResponse() {
        UUID uuid = null;
        if (verificationId != null && !verificationId.isEmpty()) {
            try {
                uuid = UUID.fromString(verificationId);
            } catch (IllegalArgumentException e) {
                // UUID 파싱 실패 시 null 유지
            }
        }

        LocalDateTime timestamp = null;
        if (verificationTimestamp != null && !verificationTimestamp.isEmpty()) {
            try {
                timestamp = LocalDateTime.parse(verificationTimestamp, DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception e) {
                // 타임스탬프 파싱 실패 시 null 유지
            }
        }

        return new PaVerificationResponse(
            status,
            uuid,
            timestamp,
            issuingCountry,
            documentNumber,
            certificateChainValidation,
            sodSignatureValidation,
            dataGroupValidation,
            (long) processingDurationMs,
            errors
        );
    }

    /**
     * 검증 결과가 유효한지 확인
     *
     * @return status가 "VALID"이면 true
     */
    public boolean isValid() {
        return "VALID".equals(status);
    }
}
