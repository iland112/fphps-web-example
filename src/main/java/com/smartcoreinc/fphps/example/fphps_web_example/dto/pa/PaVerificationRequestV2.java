package com.smartcoreinc.fphps.example.fphps_web_example.dto.pa;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * PA 검증 요청 DTO (V2 - 새로운 API Gateway용)
 * API Gateway의 POST /api/pa/verify 엔드포인트에 전송되는 요청 데이터
 *
 * 새로운 API 스펙:
 * - dataGroups 키가 숫자 문자열 ("1", "2", "14" 등)
 * - issuingCountry, documentNumber는 선택적 (SOD/DG1에서 자동 추출)
 */
public record PaVerificationRequestV2(
    String sod,
    Map<String, String> dataGroups,
    String issuingCountry,
    String documentNumber,
    String requestedBy
) {
    /**
     * 바이트 배열로부터 PA 검증 요청 생성
     *
     * @param sodBytes SOD 바이너리 데이터
     * @param dataGroupBytes Data Group 바이너리 데이터 (키: DG 번호 Integer, 값: 바이트 배열)
     * @param country 발급 국가 코드 (선택적)
     * @param docNumber 여권 번호 (선택적)
     * @param requestedBy 요청자 식별자
     * @return PaVerificationRequestV2 인스턴스
     */
    public static PaVerificationRequestV2 of(
        byte[] sodBytes,
        Map<Integer, byte[]> dataGroupBytes,
        String country,
        String docNumber,
        String requestedBy
    ) {
        String sodBase64 = Base64.getEncoder().encodeToString(sodBytes);

        // DG 번호를 숫자 문자열로 변환 (예: 1 -> "1", 2 -> "2")
        Map<String, String> dgBase64 = new HashMap<>();
        dataGroupBytes.forEach((dgNumber, value) -> {
            if (value != null && value.length > 0) {
                dgBase64.put(String.valueOf(dgNumber), Base64.getEncoder().encodeToString(value));
            }
        });

        return new PaVerificationRequestV2(
            sodBase64, dgBase64, country, docNumber, requestedBy
        );
    }

    /**
     * DG 키가 "DGx" 형식인 Map으로부터 PA 검증 요청 생성
     *
     * @param sodBytes SOD 바이너리 데이터
     * @param dataGroupBytes Data Group 바이너리 데이터 (키: "DG1", "DG2" 등, 값: 바이트 배열)
     * @param country 발급 국가 코드 (선택적)
     * @param docNumber 여권 번호 (선택적)
     * @param requestedBy 요청자 식별자
     * @return PaVerificationRequestV2 인스턴스
     */
    public static PaVerificationRequestV2 fromDgKeyFormat(
        byte[] sodBytes,
        Map<String, byte[]> dataGroupBytes,
        String country,
        String docNumber,
        String requestedBy
    ) {
        String sodBase64 = Base64.getEncoder().encodeToString(sodBytes);

        // "DGx" 형식을 숫자 문자열로 변환 (예: "DG1" -> "1", "DG14" -> "14")
        Map<String, String> dgBase64 = new HashMap<>();
        dataGroupBytes.forEach((key, value) -> {
            if (value != null && value.length > 0) {
                // "DG" 접두사 제거
                String dgNumber = key.toUpperCase().startsWith("DG")
                    ? key.substring(2)
                    : key;
                dgBase64.put(dgNumber, Base64.getEncoder().encodeToString(value));
            }
        });

        return new PaVerificationRequestV2(
            sodBase64, dgBase64, country, docNumber, requestedBy
        );
    }
}
