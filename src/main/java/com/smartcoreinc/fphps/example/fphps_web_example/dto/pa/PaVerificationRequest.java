package com.smartcoreinc.fphps.example.fphps_web_example.dto.pa;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * PA 검증 요청 DTO
 * Local PKD API의 POST /api/pa/verify 엔드포인트에 전송되는 요청 데이터
 */
public record PaVerificationRequest(
    String issuingCountry,
    String documentNumber,
    String sod,
    Map<String, String> dataGroups,
    String requestedBy
) {
    /**
     * 바이트 배열로부터 PA 검증 요청 생성
     *
     * @param country 발급 국가 코드 (ISO 3166-1 alpha-2 또는 alpha-3)
     * @param docNumber 여권 번호
     * @param sodBytes SOD 바이너리 데이터
     * @param dataGroupBytes Data Group 바이너리 데이터 (키: DG1, DG2, ..., 값: 바이트 배열)
     * @param requestedBy 요청자 식별자
     * @return PaVerificationRequest 인스턴스
     */
    public static PaVerificationRequest of(
        String country,
        String docNumber,
        byte[] sodBytes,
        Map<String, byte[]> dataGroupBytes,
        String requestedBy
    ) {
        String sodBase64 = Base64.getEncoder().encodeToString(sodBytes);

        Map<String, String> dgBase64 = new HashMap<>();
        dataGroupBytes.forEach((key, value) ->
            dgBase64.put(key, Base64.getEncoder().encodeToString(value))
        );

        return new PaVerificationRequest(
            country, docNumber, sodBase64, dgBase64, requestedBy
        );
    }
}
