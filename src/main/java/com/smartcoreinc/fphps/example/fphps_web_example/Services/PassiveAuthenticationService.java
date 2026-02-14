package com.smartcoreinc.fphps.example.fphps_web_example.Services;

import com.smartcoreinc.fphps.dto.DocumentReadResponse;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.CertificateInfo;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.ParsedSODInfo;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.pa.PaLookupRequest;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.pa.PaLookupResponse;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.pa.PaVerificationRequestV2;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.pa.PaVerificationResponse;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.pa.PaVerificationResponseV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Passive Authentication 서비스
 * API Gateway를 통해 PA 검증 수행
 */
@Slf4j
@Service
public class PassiveAuthenticationService {

    private final RestTemplate paApiRestTemplate;

    public PassiveAuthenticationService(RestTemplate paApiRestTemplate) {
        this.paApiRestTemplate = paApiRestTemplate;
    }

    /**
     * ePassport 데이터로 PA 검증 수행
     *
     * @param sodBytes SOD 바이너리 데이터
     * @param dataGroups Data Group 바이너리 데이터 (키: DG 번호, 값: 바이트 배열)
     * @param country 발급 국가 코드 (선택적)
     * @param documentNumber 여권 번호 (선택적)
     * @return PA 검증 결과
     * @throws PaVerificationException PA 검증 실패 시
     */
    public PaVerificationResponse verify(
        byte[] sodBytes,
        Map<Integer, byte[]> dataGroups,
        String country,
        String documentNumber
    ) {
        PaVerificationRequestV2 request = PaVerificationRequestV2.of(
            sodBytes,
            dataGroups,
            country,
            documentNumber,
            "fphps-web-example"
        );

        try {
            log.info("Sending PA verification request: SOD size={} bytes, DG count={}",
                sodBytes.length, dataGroups.size());

            // SOD 헤더 로그 (첫 16바이트를 hex로 출력)
            if (sodBytes.length >= 16) {
                StringBuilder hexHeader = new StringBuilder();
                for (int i = 0; i < 16; i++) {
                    hexHeader.append(String.format("%02X ", sodBytes[i]));
                }
                log.debug("SOD header (first 16 bytes): {}", hexHeader.toString().trim());
            }

            // Data Groups 크기 로그
            dataGroups.forEach((key, value) -> {
                if (value != null) {
                    log.debug("  DG{} size: {} bytes", key, value.length);
                }
            });

            // Connection: close 헤더 추가
            HttpHeaders headers = new HttpHeaders();
            headers.set("Connection", "close");
            HttpEntity<PaVerificationRequestV2> requestEntity = new HttpEntity<>(request, headers);

            // API Gateway 래퍼 응답 구조: { data: {...}, success: true/false, error: "..." }
            ResponseEntity<PaVerificationResponseV2> response = paApiRestTemplate.postForEntity(
                "/api/pa/verify",
                requestEntity,
                PaVerificationResponseV2.class
            );

            PaVerificationResponseV2 wrapper = response.getBody();

            if (wrapper == null) {
                throw new PaVerificationException("PA API returned empty response");
            }

            // 래퍼에서 에러 체크
            if (!wrapper.success()) {
                String errorMsg = wrapper.error() != null ? wrapper.error() : "Unknown error";
                log.error("PA verification failed: {}", errorMsg);
                throw new PaVerificationException("PA verification failed: " + errorMsg);
            }

            // 실제 검증 결과를 PaVerificationResponse로 변환
            PaVerificationResponse result = wrapper.toResponse();

            if (result != null) {
                log.info("PA verification completed: status={}, id={}, duration={}ms",
                    result.status(), result.verificationId(), result.processingDurationMs());
            } else {
                log.warn("PA verification returned success but no data");
            }

            return result;

        } catch (HttpClientErrorException e) {
            log.error("PA verification client error ({}): {}",
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new PaVerificationException(
                "PA verification request failed: " + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
            log.error("PA verification server error ({}): {}",
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new PaVerificationException(
                "PA API server error: " + e.getMessage(), e);
        } catch (PaVerificationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during PA verification: {}", e.getMessage(), e);
            throw new PaVerificationException(
                "Unexpected error during PA verification: " + e.getMessage(), e);
        }
    }

    /**
     * DocumentReadResponse로부터 PA 검증 수행
     *
     * @param response ePassport Reader로부터 읽은 데이터
     * @return PA 검증 결과
     * @throws PaVerificationException PA 검증 실패 시
     */
    public PaVerificationResponse verifyFromDocumentResponse(DocumentReadResponse response) {
        if (response == null) {
            throw new PaVerificationException("DocumentReadResponse is null");
        }

        // SOD 추출
        byte[] sodBytes = response.getSodDataBytes();
        if (sodBytes == null || sodBytes.length == 0) {
            throw new PaVerificationException("SOD data is missing in DocumentReadResponse");
        }

        // Data Groups 추출 (Integer 키 형식)
        Map<Integer, byte[]> dataGroups = response.getDgDataMap();
        if (dataGroups == null || dataGroups.isEmpty()) {
            throw new PaVerificationException("No Data Groups found in DocumentReadResponse");
        }

        // MRZ에서 국가 코드 및 여권 번호 추출
        String country = extractIssuingCountry(response);
        String documentNumber = extractDocumentNumber(response);

        log.debug("Extracted from DocumentReadResponse: country={}, docNumber={}, DGs={}",
            country, documentNumber, dataGroups.keySet());

        return verify(sodBytes, dataGroups, country, documentNumber);
    }

    /**
     * PA Lookup: DSC Subject DN / SHA-256 Fingerprint 기반 Trust Chain 조회
     *
     * @param response ePassport Reader로부터 읽은 데이터
     * @return PA Lookup 결과
     * @throws PaVerificationException 조회 실패 시
     */
    public PaLookupResponse paLookup(DocumentReadResponse response) {
        if (response == null) {
            throw new PaVerificationException("DocumentReadResponse is null");
        }

        if (response.getParsedSOD() == null) {
            throw new PaVerificationException("No SOD data available. Please read passport first.");
        }

        // ParsedSOD에서 DSC 인증서 정보 추출
        ParsedSODInfo sodInfo = ParsedSODInfo.from(response.getParsedSOD());
        CertificateInfo dscCert = sodInfo.getDscCertificate();

        if (dscCert == null) {
            throw new PaVerificationException("DSC certificate not found in SOD data");
        }

        // Subject DN
        String subjectDn = dscCert.getSubject();

        // SHA-256 Fingerprint: "AA:BB:CC:..." → "aabbcc..." (콜론 제거, 소문자 변환)
        String fingerprint = null;
        if (dscCert.getSha256Fingerprint() != null && !"N/A".equals(dscCert.getSha256Fingerprint())) {
            fingerprint = dscCert.getSha256Fingerprint().replace(":", "").toLowerCase();
        }

        log.info("PA Lookup: subjectDn={}, fingerprint={}", subjectDn,
            fingerprint != null ? fingerprint.substring(0, Math.min(16, fingerprint.length())) + "..." : "null");

        PaLookupRequest request = new PaLookupRequest(subjectDn, fingerprint);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Connection", "close");
            HttpEntity<PaLookupRequest> requestEntity = new HttpEntity<>(request, headers);

            ResponseEntity<PaLookupResponse> result = paApiRestTemplate.postForEntity(
                "/api/certificates/pa-lookup",
                requestEntity,
                PaLookupResponse.class
            );

            PaLookupResponse lookupResponse = result.getBody();
            if (lookupResponse == null) {
                throw new PaVerificationException("PA Lookup API returned empty response");
            }

            log.info("PA Lookup completed: success={}, validation={}",
                lookupResponse.success(),
                lookupResponse.validation() != null ? lookupResponse.validation().validationStatus() : "null");

            return lookupResponse;

        } catch (HttpClientErrorException e) {
            log.error("PA Lookup client error ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new PaVerificationException("PA Lookup request failed: " + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
            log.error("PA Lookup server error ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new PaVerificationException("PA Lookup server error: " + e.getMessage(), e);
        } catch (PaVerificationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during PA Lookup: {}", e.getMessage(), e);
            throw new PaVerificationException("Unexpected error during PA Lookup: " + e.getMessage(), e);
        }
    }

    /**
     * MRZ 데이터에서 발급 국가 코드 추출
     */
    private String extractIssuingCountry(DocumentReadResponse response) {
        if (response.getMrzInfo() != null
            && response.getMrzInfo().getIssuingState() != null) {
            return response.getMrzInfo().getIssuingState();
        }
        log.warn("Issuing country not found in MRZ data");
        return "UNKNOWN";
    }

    /**
     * MRZ 데이터에서 여권 번호 추출
     */
    private String extractDocumentNumber(DocumentReadResponse response) {
        if (response.getMrzInfo() != null
            && response.getMrzInfo().getPassportNumber() != null) {
            return response.getMrzInfo().getPassportNumber();
        }
        log.warn("Document number not found in MRZ data");
        return "UNKNOWN";
    }

    /**
     * PA 검증 예외
     */
    public static class PaVerificationException extends RuntimeException {
        public PaVerificationException(String message) {
            super(message);
        }

        public PaVerificationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
