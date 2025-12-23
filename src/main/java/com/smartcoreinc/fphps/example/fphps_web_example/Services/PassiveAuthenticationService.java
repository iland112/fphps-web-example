package com.smartcoreinc.fphps.example.fphps_web_example.Services;

import com.smartcoreinc.fphps.dto.DocumentReadResponse;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.pa.PaVerificationRequest;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.pa.PaVerificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class PassiveAuthenticationService {

    private final RestTemplate paApiRestTemplate;

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
