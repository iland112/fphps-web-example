package com.smartcoreinc.fphps.example.fphps_web_example.Services;

import com.smartcoreinc.fphps.dto.DocumentReadResponse;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.pa.PaVerificationRequest;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.pa.PaVerificationRequestV2;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.pa.PaVerificationResponse;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.pa.PaVerificationResponseV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Passive Authentication 서비스
 * Local PKD API를 호출하여 전자여권 PA 검증 수행
 */
@Slf4j
@Service
public class PassiveAuthenticationService {

    private final RestTemplate paApiRestTemplate;
    private final RestTemplate paApiRestTemplateV2;

    public PassiveAuthenticationService(
        @Qualifier("paApiRestTemplate") RestTemplate paApiRestTemplate,
        @Qualifier("paApiRestTemplateV2") RestTemplate paApiRestTemplateV2
    ) {
        this.paApiRestTemplate = paApiRestTemplate;
        this.paApiRestTemplateV2 = paApiRestTemplateV2;
    }

    /**
     * ePassport 데이터로 PA 검증 수행
     *
     * @param country 발급 국가 코드
     * @param documentNumber 여권 번호
     * @param sodBytes SOD 바이너리 데이터
     * @param dataGroups Data Group 바이너리 데이터
     * @return PA 검증 결과
     * @throws PaVerificationException PA 검증 실패 시
     */
    public PaVerificationResponse verify(
        String country,
        String documentNumber,
        byte[] sodBytes,
        Map<String, byte[]> dataGroups
    ) {
        PaVerificationRequest request = PaVerificationRequest.of(
            country,
            documentNumber,
            sodBytes,
            dataGroups,
            "fphps-web-example"
        );

        try {
            log.info("Sending PA verification request for country={}, docNumber={}, SOD size={} bytes, DG count={}",
                country, documentNumber, sodBytes.length, dataGroups.size());

            // SOD 헤더 로그 (첫 16바이트를 hex로 출력하여 형식 확인)
            if (sodBytes.length >= 16) {
                StringBuilder hexHeader = new StringBuilder();
                for (int i = 0; i < 16; i++) {
                    hexHeader.append(String.format("%02X ", sodBytes[i]));
                }
                log.info("SOD header (first 16 bytes): {}", hexHeader.toString().trim());
            }

            // Base64 인코딩된 SOD 확인
            log.info("SOD Base64 length: {} chars, first 50 chars: {}",
                request.sod().length(),
                request.sod().substring(0, Math.min(50, request.sod().length())));

            // Data Groups 크기 로그
            dataGroups.forEach((key, value) -> {
                if (value != null) {
                    log.debug("  {} size: {} bytes", key, value.length);
                }
            });

            // Connection: close 헤더 추가로 Connection Reset 방지
            HttpHeaders headers = new HttpHeaders();
            headers.set("Connection", "close");
            HttpEntity<PaVerificationRequest> requestEntity = new HttpEntity<>(request, headers);

            ResponseEntity<PaVerificationResponse> response = paApiRestTemplate.postForEntity(
                "/api/pa/verify",
                requestEntity,
                PaVerificationResponse.class
            );

            PaVerificationResponse result = response.getBody();

            if (result != null) {
                log.info("PA verification completed: status={}, id={}, duration={}ms",
                    result.status(), result.verificationId(), result.processingDurationMs());
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
        byte[] sodBytes = extractSOD(response);
        if (sodBytes == null || sodBytes.length == 0) {
            throw new PaVerificationException("SOD data is missing in DocumentReadResponse");
        }

        // Data Groups 추출
        Map<String, byte[]> dataGroups = extractDataGroups(response);
        if (dataGroups.isEmpty()) {
            throw new PaVerificationException("No Data Groups found in DocumentReadResponse");
        }

        // MRZ에서 국가 코드 및 여권 번호 추출
        String country = extractIssuingCountry(response);
        String documentNumber = extractDocumentNumber(response);

        log.debug("Extracted from DocumentReadResponse: country={}, docNumber={}, DGs={}",
            country, documentNumber, dataGroups.keySet());

        return verify(country, documentNumber, sodBytes, dataGroups);
    }

    /**
     * DocumentReadResponse에서 SOD 바이너리 추출
     */
    private byte[] extractSOD(DocumentReadResponse response) {
        byte[] sodBytes = response.getSodDataBytes();
        if (sodBytes == null || sodBytes.length == 0) {
            log.warn("SOD data not found in DocumentReadResponse");
        }
        return sodBytes;
    }

    /**
     * DocumentReadResponse에서 Data Groups 추출
     * dgDataMap의 키(Integer)를 "DG1", "DG2" 형식의 문자열로 변환
     */
    private Map<String, byte[]> extractDataGroups(DocumentReadResponse response) {
        Map<String, byte[]> dataGroups = new HashMap<>();

        Map<Integer, byte[]> dgDataMap = response.getDgDataMap();
        if (dgDataMap != null && !dgDataMap.isEmpty()) {
            dgDataMap.forEach((dgNumber, dgBytes) -> {
                String dgKey = "DG" + dgNumber;
                dataGroups.put(dgKey, dgBytes);
            });
        }

        log.debug("Extracted {} Data Groups: {}", dataGroups.size(), dataGroups.keySet());
        return dataGroups;
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

    // ==================== V2 API (API Gateway) ====================

    /**
     * ePassport 데이터로 PA 검증 수행 (V2 - API Gateway)
     * 새로운 API Gateway를 통해 PA 검증 수행
     *
     * @param sodBytes SOD 바이너리 데이터
     * @param dataGroups Data Group 바이너리 데이터 (키: DG 번호, 값: 바이트 배열)
     * @param country 발급 국가 코드 (선택적)
     * @param documentNumber 여권 번호 (선택적)
     * @return PA 검증 결과
     * @throws PaVerificationException PA 검증 실패 시
     */
    public PaVerificationResponse verifyV2(
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
            log.info("Sending PA V2 verification request: SOD size={} bytes, DG count={}",
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

            // V2 API는 래퍼 응답 구조 사용: { data: {...}, success: true/false, error: "..." }
            ResponseEntity<PaVerificationResponseV2> response = paApiRestTemplateV2.postForEntity(
                "/api/pa/verify",
                requestEntity,
                PaVerificationResponseV2.class
            );

            PaVerificationResponseV2 wrapper = response.getBody();

            if (wrapper == null) {
                throw new PaVerificationException("PA V2 API returned empty response");
            }

            // 래퍼에서 에러 체크
            if (!wrapper.success()) {
                String errorMsg = wrapper.error() != null ? wrapper.error() : "Unknown error";
                log.error("PA V2 verification failed: {}", errorMsg);
                throw new PaVerificationException("PA V2 verification failed: " + errorMsg);
            }

            // 실제 검증 결과를 PaVerificationResponse로 변환
            PaVerificationResponse result = wrapper.toResponse();

            if (result != null) {
                log.info("PA V2 verification completed: status={}, id={}, duration={}ms",
                    result.status(), result.verificationId(), result.processingDurationMs());
            } else {
                log.warn("PA V2 verification returned success but no data");
            }

            return result;

        } catch (HttpClientErrorException e) {
            log.error("PA V2 verification client error ({}): {}",
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new PaVerificationException(
                "PA V2 verification request failed: " + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
            log.error("PA V2 verification server error ({}): {}",
                e.getStatusCode(), e.getResponseBodyAsString());
            throw new PaVerificationException(
                "PA API V2 server error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during PA V2 verification: {}", e.getMessage(), e);
            throw new PaVerificationException(
                "Unexpected error during PA V2 verification: " + e.getMessage(), e);
        }
    }

    /**
     * DocumentReadResponse로부터 PA 검증 수행 (V2 - API Gateway)
     *
     * @param response ePassport Reader로부터 읽은 데이터
     * @return PA 검증 결과
     * @throws PaVerificationException PA 검증 실패 시
     */
    public PaVerificationResponse verifyFromDocumentResponseV2(DocumentReadResponse response) {
        if (response == null) {
            throw new PaVerificationException("DocumentReadResponse is null");
        }

        // SOD 추출
        byte[] sodBytes = extractSOD(response);
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

        log.debug("Extracted for V2: country={}, docNumber={}, DGs={}",
            country, documentNumber, dataGroups.keySet());

        return verifyV2(sodBytes, dataGroups, country, documentNumber);
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
