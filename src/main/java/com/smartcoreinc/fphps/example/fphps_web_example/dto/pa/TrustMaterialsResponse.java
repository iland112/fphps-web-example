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
    TrustMaterialsData data,
    String error
) {
    /**
     * requestId는 data 안에 있으므로 편의 메서드 제공
     */
    public String requestId() {
        return data != null ? data.requestId() : null;
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
