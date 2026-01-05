package com.smartcoreinc.fphps.example.fphps_web_example.dto.pa;

/**
 * PA V2 API 응답 래퍼 DTO
 * API Gateway의 POST /api/pa/verify 엔드포인트로부터 받는 응답 데이터
 *
 * 응답 구조:
 * {
 *   "data": { ... 실제 검증 결과 ... },
 *   "success": true/false,
 *   "error": "에러 메시지" (선택)
 * }
 */
public record PaVerificationResponseV2(
    PaVerificationData data,
    boolean success,
    String error
) {
    /**
     * 실제 PA 검증 데이터를 PaVerificationResponse로 변환
     * 기존 V1 응답 형식과 호환성 유지를 위해 사용
     *
     * @return PaVerificationResponse 또는 null (데이터가 없는 경우)
     */
    public PaVerificationResponse toResponse() {
        if (data == null) {
            return null;
        }
        return data.toResponse();
    }

    /**
     * API 호출이 성공했는지 확인
     *
     * @return success가 true이고 data가 존재하면 true
     */
    public boolean isSuccessful() {
        return success && data != null;
    }

    /**
     * 에러 메시지 반환
     *
     * @return 에러 메시지 또는 null
     */
    public String getErrorMessage() {
        return error;
    }
}
