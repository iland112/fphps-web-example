package com.smartcoreinc.fphps.example.fphps_web_example.dto.pa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 클라이언트 PA Step 1: Trust Materials 응답 DTO
 * POST /api/pa/trust-materials 응답
 *
 * 실제 응답 구조:
 * {
 *   "success": true,
 *   "data": {
 *     "requestId": "...",
 *     "countryCode": "KR",
 *     "csca": [...],
 *     "linkCertificates": [...],
 *     "crl": [...],
 *     "processingTimeMs": 4,
 *     "timestamp": "..."
 *   }
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TrustMaterialsResponse(
    boolean success,
    @JsonProperty("requestId") String topLevelRequestId,  // PA API 응답에서 requestId가 최상위 필드인 경우
    TrustMaterialsData data,
    String error
) {
    /**
     * requestId 접근자 — 최상위 필드 우선, 없으면 data 내부에서 조회
     * PA API 문서 예제: trust["requestId"] (최상위) + trust["data"]["csca"]
     */
    public String requestId() {
        if (topLevelRequestId != null && !topLevelRequestId.isBlank()) {
            return topLevelRequestId;
        }
        return data != null ? data.requestId() : null;
    }

    /**
     * 하위 호환성 생성자 (3-arg) — 내부 캐시 폴백 생성 시 사용
     */
    public TrustMaterialsResponse(boolean success, TrustMaterialsData data, String error) {
        this(success, null, data, error);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TrustMaterialsData(
        String requestId,
        String countryCode,
        List<CscaCert> csca,
        @JsonProperty("linkCertificates") List<LinkCert> linkCertificates,
        List<CrlEntry> crl,
        Integer processingTimeMs,
        String timestamp
    ) {
        /**
         * linkCertificates 편의 접근자 (linkCert 호환)
         */
        public List<LinkCert> linkCert() {
            return linkCertificates;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CscaCert(
        String subjectDn,
        String issuerDn,
        String derBase64,
        String notBefore,
        String notAfter
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LinkCert(
        String subjectDn,
        String issuerDn,
        String derBase64,
        String notBefore,
        String notAfter
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CrlEntry(
        String issuerDn,
        String derBase64,
        String thisUpdate,
        String nextUpdate
    ) {}
}
