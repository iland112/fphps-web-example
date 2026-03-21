package com.smartcoreinc.fphps.example.fphps_web_example.Services;

import com.smartcoreinc.fphps.dto.DocumentReadResponse;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.CertificateInfo;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.ParsedSODInfo;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.pa.ClientPaReportRequest;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.pa.ClientPaResult;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.pa.TrustMaterialsRequest;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.pa.TrustMaterialsResponse;
import com.smartcoreinc.fphps.sod.ParsedSOD;
import com.smartcoreinc.fphps.sod.SODSignatureVerifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.*;

/**
 * 클라이언트 모드 PA 검증 서비스 (v2.1.14+)
 *
 * 최적화:
 * - Trust Materials 캐시 (국가별, TTL 1시간)
 * - 로컬 검증과 Trust Materials 다운로드 병렬 수행
 * - 결과 보고 비동기 수행
 */
@Slf4j
@Service
public class ClientPaVerificationService {

    private final RestTemplate paApiRestTemplate;
    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    // Trust Materials 캐시 (국가코드 → 캐시 항목)
    private final ConcurrentHashMap<String, CachedTrustMaterials> tmCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 60 * 60 * 1000; // 1시간

    public ClientPaVerificationService(RestTemplate paApiRestTemplate) {
        this.paApiRestTemplate = paApiRestTemplate;
    }

    /**
     * 클라이언트 PA 검증 전체 수행 (최적화)
     *
     * 병렬 실행:
     * - Thread 1: Trust Materials 다운로드 (또는 캐시 히트)
     * - Thread 2: SOD 서명 검증 + DG 해시 검증 (로컬, 네트워크 불필요)
     *
     * 비동기:
     * - 결과 보고는 백그라운드에서 수행 (UI 응답 즉시 반환)
     */
    public ClientPaResult verify(DocumentReadResponse response) {
        long startTime = System.currentTimeMillis();
        List<String> errors = Collections.synchronizedList(new ArrayList<>());

        if (response == null) {
            throw new PassiveAuthenticationService.PaVerificationException(
                "DocumentReadResponse is null");
        }

        ParsedSOD parsedSOD = response.getParsedSOD();
        if (parsedSOD == null) {
            throw new PassiveAuthenticationService.PaVerificationException(
                "No SOD data available. Please read passport first.");
        }

        Map<Integer, byte[]> dgDataMap = response.getDgDataMap();
        if (dgDataMap == null || dgDataMap.isEmpty()) {
            throw new PassiveAuthenticationService.PaVerificationException(
                "No Data Groups found in DocumentReadResponse");
        }

        String countryCode = extractCountryCode(response);

        // ── 병렬 실행: Trust Materials 다운로드 + 로컬 검증 ──
        Future<TrustMaterialsResponse> tmFuture = executor.submit(
            () -> getOrDownloadTrustMaterials(countryCode, parsedSOD));

        // 로컬 검증은 메인 스레드에서 즉시 시작 (네트워크 불필요)
        ClientPaResult.SodSignatureResult sodSigResult = verifySodSignature(parsedSOD, errors);
        ClientPaResult.DgHashResult dgHashResult = verifyDgHashes(parsedSOD, dgDataMap, errors);
        ClientPaResult.DscInfo dscInfo = extractDscInfo(parsedSOD);

        // Trust Materials 결과 대기
        String requestId = null;
        TrustMaterialsResponse trustMaterials = null;
        ClientPaResult.TrustMaterialsInfo tmInfo = null;
        boolean cacheHit = false;

        try {
            trustMaterials = tmFuture.get(15, TimeUnit.SECONDS);
            if (trustMaterials != null && trustMaterials.success()) {
                requestId = trustMaterials.requestId();
                var data = trustMaterials.data();
                tmInfo = new ClientPaResult.TrustMaterialsInfo(
                    data != null && data.csca() != null ? data.csca().size() : 0,
                    data != null && data.linkCert() != null ? data.linkCert().size() : 0,
                    data != null && data.crl() != null ? data.crl().size() : 0,
                    countryCode
                );
                CachedTrustMaterials cached = tmCache.get(countryCode);
                cacheHit = cached != null && !cached.isExpired();
            }
        } catch (Exception e) {
            log.warn("Trust Materials download failed: {}", e.getMessage());
            errors.add("Trust Materials download failed: " + e.getMessage());
        }

        // Trust Chain 검증 + CRL 체크 (Trust Materials 필요)
        ClientPaResult.TrustChainResult trustChainResult = verifyTrustChainLocally(
            parsedSOD, trustMaterials, errors);
        ClientPaResult.CrlCheckResult crlCheckResult = checkCrlLocally(
            parsedSOD, trustMaterials, errors);

        String overallStatus = determineOverallStatus(
            sodSigResult, dgHashResult, trustChainResult, crlCheckResult);

        long duration = System.currentTimeMillis() - startTime;

        // ── 결과 보고 (비동기) ──
        final String finalRequestId = requestId;
        final String finalStatus = overallStatus;
        CompletableFuture<Boolean> reportFuture = CompletableFuture.supplyAsync(() -> {
            if (finalRequestId == null) return false;
            try {
                reportResult(finalRequestId, finalStatus, sodSigResult, dgHashResult,
                    trustChainResult, crlCheckResult, (int) duration);
                log.info("Client PA result reported: requestId={}, status={}", finalRequestId, finalStatus);
                return true;
            } catch (Exception e) {
                log.warn("Failed to report client PA result: {}", e.getMessage());
                return false;
            }
        }, executor);

        // 보고 결과를 짧게 대기 (최대 2초)
        boolean serverReported = false;
        String serverReportError = null;
        try {
            serverReported = reportFuture.get(2, TimeUnit.SECONDS);
            if (!serverReported && finalRequestId != null) {
                serverReportError = "Report request failed";
            }
        } catch (TimeoutException e) {
            // 2초 내 완료되지 않으면 백그라운드에서 계속 수행
            serverReported = true; // 진행 중으로 표시
            serverReportError = null;
            log.debug("Result report still in progress (background)");
        } catch (Exception e) {
            serverReportError = e.getMessage();
        }

        if (finalRequestId == null) {
            serverReportError = "No requestId (Trust Materials download failed)";
        }

        log.info("Client PA completed: status={}, duration={}ms, cache={}, " +
                "sodSig={}, dgHash={}/{}, trustChain={}, crl={}",
            overallStatus, duration, cacheHit ? "HIT" : "MISS",
            sodSigResult.valid(),
            dgHashResult.validGroups(), dgHashResult.totalGroups(),
            trustChainResult.valid(), crlCheckResult.passed());

        return new ClientPaResult(
            "CLIENT", overallStatus, duration, requestId,
            sodSigResult, dgHashResult, trustChainResult, crlCheckResult,
            tmInfo, dscInfo, serverReported, serverReportError,
            errors.isEmpty() ? null : errors
        );
    }

    // ================================================================
    // Trust Materials 캐시
    // ================================================================

    private TrustMaterialsResponse getOrDownloadTrustMaterials(
            String countryCode, ParsedSOD parsedSOD) {

        CachedTrustMaterials cached = tmCache.get(countryCode);
        if (cached != null && !cached.isExpired()) {
            log.debug("Trust Materials cache HIT for country={}", countryCode);
            // 캐시 히트 시에도 새 requestId 발급을 위해 서버에 요청
            // 단, 캐시된 CSCA/CRL 데이터를 재사용
            try {
                TrustMaterialsResponse fresh = downloadTrustMaterials(countryCode, parsedSOD);
                if (fresh != null && fresh.success()) {
                    // 새 requestId + 캐시된 데이터 조합
                    tmCache.put(countryCode, new CachedTrustMaterials(fresh));
                    return fresh;
                }
            } catch (Exception e) {
                log.debug("Fresh download failed, using cached data: {}", e.getMessage());
                return cached.response;
            }
        }

        log.debug("Trust Materials cache MISS for country={}, downloading...", countryCode);
        TrustMaterialsResponse response = downloadTrustMaterials(countryCode, parsedSOD);
        if (response != null && response.success()) {
            tmCache.put(countryCode, new CachedTrustMaterials(response));
        }
        return response;
    }

    private TrustMaterialsResponse downloadTrustMaterials(
            String countryCode, ParsedSOD parsedSOD) {
        String dscIssuerDn = null;
        if (parsedSOD.dscCertificate() != null) {
            dscIssuerDn = parsedSOD.dscCertificate().getIssuerX500Principal().getName();
        }

        TrustMaterialsRequest request = new TrustMaterialsRequest(
            countryCode, dscIssuerDn, "fphps-web-example");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Connection", "close");
        HttpEntity<TrustMaterialsRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<TrustMaterialsResponse> resp = paApiRestTemplate.postForEntity(
            "/api/pa/trust-materials", entity, TrustMaterialsResponse.class);
        return resp.getBody();
    }

    private static class CachedTrustMaterials {
        final TrustMaterialsResponse response;
        final long cachedAt;

        CachedTrustMaterials(TrustMaterialsResponse response) {
            this.response = response;
            this.cachedAt = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - cachedAt > CACHE_TTL_MS;
        }
    }

    // ================================================================
    // SOD 서명 검증 (로컬)
    // ================================================================

    private ClientPaResult.SodSignatureResult verifySodSignature(
            ParsedSOD parsedSOD, List<String> errors) {
        String hashAlg = ParsedSODInfo.from(parsedSOD).getDigestAlgorithmName();
        String sigAlg = parsedSOD.dscCertificate() != null
            ? parsedSOD.dscCertificate().getSigAlgName() : "Unknown";

        try {
            SODSignatureVerifier.verifySignature(
                parsedSOD.signerInformation(), parsedSOD.dscCertificate());
            return new ClientPaResult.SodSignatureResult(true, hashAlg, sigAlg, null);
        } catch (Exception e) {
            errors.add("SOD signature verification failed: " + e.getMessage());
            return new ClientPaResult.SodSignatureResult(false, hashAlg, sigAlg, e.getMessage());
        }
    }

    // ================================================================
    // DG 해시 검증 (로컬)
    // ================================================================

    private ClientPaResult.DgHashResult verifyDgHashes(
            ParsedSOD parsedSOD, Map<Integer, byte[]> dgDataMap, List<String> errors) {

        Map<Integer, byte[]> sodHashes = parsedSOD.dgHashes();
        String digestAlgOid = parsedSOD.digestAlgorithmOid();

        Map<String, ClientPaResult.DgHashDetail> details = new LinkedHashMap<>();
        int validCount = 0, invalidCount = 0, skippedCount = 0;

        for (Map.Entry<Integer, byte[]> entry : new TreeMap<>(sodHashes).entrySet()) {
            int dgNum = entry.getKey();
            byte[] expectedHash = entry.getValue();
            String dgName = "DG" + dgNum;

            byte[] dgData = dgDataMap.get(dgNum);
            if (dgData == null) {
                details.put(dgName, new ClientPaResult.DgHashDetail(
                    true, true, bytesToHex(expectedHash), "N/A (not read)"));
                skippedCount++;
                continue;
            }

            try {
                MessageDigest md = MessageDigest.getInstance(oidToJcaName(digestAlgOid));
                byte[] actualHash = md.digest(dgData);
                boolean match = MessageDigest.isEqual(expectedHash, actualHash);
                details.put(dgName, new ClientPaResult.DgHashDetail(
                    match, false, bytesToHex(expectedHash), bytesToHex(actualHash)));
                if (match) validCount++;
                else { invalidCount++; errors.add("DG" + dgNum + " hash mismatch"); }
            } catch (Exception e) {
                details.put(dgName, new ClientPaResult.DgHashDetail(
                    false, false, bytesToHex(expectedHash), "Error: " + e.getMessage()));
                invalidCount++;
            }
        }

        return new ClientPaResult.DgHashResult(
            validCount + invalidCount, validCount, invalidCount, skippedCount, details);
    }

    // ================================================================
    // Trust Chain 검증 (로컬)
    // ================================================================

    private ClientPaResult.TrustChainResult verifyTrustChainLocally(
            ParsedSOD parsedSOD, TrustMaterialsResponse trustMaterials, List<String> errors) {

        if (trustMaterials == null || !trustMaterials.success() || trustMaterials.data() == null) {
            return new ClientPaResult.TrustChainResult(
                false, false, false, null, null, "Trust Materials not available");
        }

        X509Certificate dscCert = parsedSOD.dscCertificate();
        if (dscCert == null) {
            return new ClientPaResult.TrustChainResult(
                true, false, false, null, null, "DSC certificate not found in SOD");
        }

        var cscaList = trustMaterials.data().csca();
        if (cscaList == null || cscaList.isEmpty()) {
            return new ClientPaResult.TrustChainResult(
                true, false, false, null, null, "No CSCA certificates available");
        }

        CertificateFactory cf = getCertificateFactory();

        // DSC → CSCA 직접 서명 검증
        for (var cscaEntry : cscaList) {
            try {
                byte[] cscaDer = Base64.getDecoder().decode(cscaEntry.derBase64());
                X509Certificate cscaCert = (X509Certificate) cf.generateCertificate(
                    new ByteArrayInputStream(cscaDer));
                dscCert.verify(cscaCert.getPublicKey());

                return new ClientPaResult.TrustChainResult(
                    true, true, true,
                    cscaCert.getSubjectX500Principal().getName(),
                    "DSC → CSCA", null);
            } catch (Exception e) {
                log.debug("DSC not signed by CSCA {}: {}", cscaEntry.subjectDn(), e.getMessage());
            }
        }

        // Link Certificate를 통한 검증
        var linkCerts = trustMaterials.data().linkCert();
        if (linkCerts != null && !linkCerts.isEmpty()) {
            for (var linkEntry : linkCerts) {
                try {
                    byte[] linkDer = Base64.getDecoder().decode(linkEntry.derBase64());
                    X509Certificate linkCert = (X509Certificate) cf.generateCertificate(
                        new ByteArrayInputStream(linkDer));

                    dscCert.verify(linkCert.getPublicKey());

                    for (var cscaEntry : cscaList) {
                        try {
                            byte[] cscaDer = Base64.getDecoder().decode(cscaEntry.derBase64());
                            X509Certificate cscaCert = (X509Certificate) cf.generateCertificate(
                                new ByteArrayInputStream(cscaDer));
                            linkCert.verify(cscaCert.getPublicKey());

                            return new ClientPaResult.TrustChainResult(
                                true, true, true,
                                cscaCert.getSubjectX500Principal().getName(),
                                "DSC → Link → CSCA", null);
                        } catch (Exception ignored) {}
                    }
                } catch (Exception ignored) {}
            }
        }

        errors.add("Trust chain verification failed: DSC not signed by any available CSCA");
        return new ClientPaResult.TrustChainResult(
            true, false, true, null, null,
            "DSC not signed by any available CSCA");
    }

    // ================================================================
    // CRL 체크 (로컬)
    // ================================================================

    private ClientPaResult.CrlCheckResult checkCrlLocally(
            ParsedSOD parsedSOD, TrustMaterialsResponse trustMaterials, List<String> errors) {

        if (trustMaterials == null || !trustMaterials.success() || trustMaterials.data() == null) {
            return new ClientPaResult.CrlCheckResult(false, true, false, null,
                "Trust Materials not available");
        }

        var crlList = trustMaterials.data().crl();
        if (crlList == null || crlList.isEmpty()) {
            return new ClientPaResult.CrlCheckResult(false, true, false, null, "No CRL available");
        }

        X509Certificate dscCert = parsedSOD.dscCertificate();
        if (dscCert == null) {
            return new ClientPaResult.CrlCheckResult(false, true, false, null, "DSC not found");
        }

        CertificateFactory cf = getCertificateFactory();

        for (var crlEntry : crlList) {
            try {
                byte[] crlDer = Base64.getDecoder().decode(crlEntry.derBase64());
                X509CRL crl = (X509CRL) cf.generateCRL(new ByteArrayInputStream(crlDer));
                boolean isRevoked = crl.isRevoked(dscCert);

                if (isRevoked) {
                    errors.add("DSC certificate is REVOKED");
                }
                return new ClientPaResult.CrlCheckResult(
                    true, !isRevoked, isRevoked, crlEntry.issuerDn(), null);
            } catch (Exception e) {
                log.debug("CRL check failed for {}: {}", crlEntry.issuerDn(), e.getMessage());
            }
        }

        return new ClientPaResult.CrlCheckResult(false, true, false, null, "CRL parsing failed");
    }

    // ================================================================
    // DSC 인증서 정보 추출
    // ================================================================

    private ClientPaResult.DscInfo extractDscInfo(ParsedSOD parsedSOD) {
        X509Certificate cert = parsedSOD.dscCertificate();
        if (cert == null) return null;

        CertificateInfo certInfo = CertificateInfo.from(cert);
        String sha256 = null;
        if (certInfo.getSha256Fingerprint() != null
                && !"N/A".equals(certInfo.getSha256Fingerprint())) {
            sha256 = certInfo.getSha256Fingerprint().replace(":", "").toLowerCase();
        }

        return new ClientPaResult.DscInfo(
            certInfo.getSubject(), certInfo.getIssuer(), certInfo.getSerialNumber(),
            certInfo.getNotBefore(), certInfo.getNotAfter(),
            cert.getNotAfter().before(new Date()),
            certInfo.getSignatureAlgorithm(), certInfo.getPublicKeyAlgorithm(),
            certInfo.getPublicKeySize(), sha256);
    }

    // ================================================================
    // 종합 상태 결정
    // ================================================================

    private String determineOverallStatus(
            ClientPaResult.SodSignatureResult sodSig,
            ClientPaResult.DgHashResult dgHash,
            ClientPaResult.TrustChainResult trustChain,
            ClientPaResult.CrlCheckResult crlCheck) {

        if (!sodSig.valid() || dgHash.invalidGroups() > 0) return "INVALID";
        if (crlCheck.revoked()) return "INVALID";
        if (!trustChain.available()) return "PARTIAL";
        if (!trustChain.valid()) return "INVALID";
        return "VALID";
    }

    // ================================================================
    // 결과 보고
    // ================================================================

    private void reportResult(String requestId, String overallStatus,
            ClientPaResult.SodSignatureResult sodSig,
            ClientPaResult.DgHashResult dgHash,
            ClientPaResult.TrustChainResult trustChain,
            ClientPaResult.CrlCheckResult crlCheck,
            int processingTimeMs) {

        ClientPaReportRequest report = new ClientPaReportRequest(
            requestId, overallStatus, null,
            trustChain.valid(), sodSig.valid(),
            dgHash.invalidGroups() == 0,
            crlCheck.checked() && crlCheck.passed(),
            processingTimeMs, null);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Connection", "close");
        HttpEntity<ClientPaReportRequest> entity = new HttpEntity<>(report, headers);

        paApiRestTemplate.postForEntity("/api/pa/trust-materials/result", entity, Map.class);
    }

    // ================================================================
    // 유틸리티
    // ================================================================

    private CertificateFactory getCertificateFactory() {
        try {
            return CertificateFactory.getInstance("X.509", "BC");
        } catch (Exception e) {
            try { return CertificateFactory.getInstance("X.509"); }
            catch (Exception ex) { throw new RuntimeException("CertificateFactory init failed", ex); }
        }
    }

    private String extractCountryCode(DocumentReadResponse response) {
        if (response.getMrzInfo() != null && response.getMrzInfo().getIssuingState() != null) {
            String state = response.getMrzInfo().getIssuingState().trim();
            if (state.length() == 3) return iso3to2(state);
            return state;
        }
        return "XX";
    }

    private String iso3to2(String iso3) {
        return switch (iso3.toUpperCase()) {
            case "KOR" -> "KR"; case "USA" -> "US"; case "JPN" -> "JP";
            case "CHN" -> "CN"; case "GBR" -> "GB"; case "DEU" -> "DE";
            case "FRA" -> "FR"; case "ARE" -> "AE"; case "THA" -> "TH";
            case "VNM" -> "VN"; case "PHL" -> "PH"; case "IDN" -> "ID";
            case "MYS" -> "MY"; case "SGP" -> "SG"; case "AUS" -> "AU";
            case "CAN" -> "CA"; case "IND" -> "IN";
            default -> iso3.substring(0, 2);
        };
    }

    private String oidToJcaName(String oid) {
        if (oid == null) return "SHA-256";
        return switch (oid) {
            case "1.3.14.3.2.26" -> "SHA-1";
            case "2.16.840.1.101.3.4.2.1" -> "SHA-256";
            case "2.16.840.1.101.3.4.2.2" -> "SHA-384";
            case "2.16.840.1.101.3.4.2.3" -> "SHA-512";
            case "2.16.840.1.101.3.4.2.4" -> "SHA-224";
            default -> "SHA-256";
        };
    }

    private static String bytesToHex(byte[] bytes) {
        if (bytes == null) return "";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
