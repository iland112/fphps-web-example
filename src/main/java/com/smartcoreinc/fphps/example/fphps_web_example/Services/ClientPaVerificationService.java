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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

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
    private final PaApiSettingsService paApiSettingsService;
    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    // Trust Materials 캐시 (국가코드 → 캐시 항목)
    private final ConcurrentHashMap<String, CachedTrustMaterials> tmCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 60 * 60 * 1000; // 1시간

    public ClientPaVerificationService(@Qualifier("paApiRestTemplate") RestTemplate paApiRestTemplate,
                                       PaApiSettingsService paApiSettingsService) {
        this.paApiRestTemplate = paApiRestTemplate;
        this.paApiSettingsService = paApiSettingsService;
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
        // cacheHit: 다운로드 시작 전 캐시 상태 확인 (다운로드 후 put으로 덮어씌워지므로 사전 확인 필요)
        boolean cacheHit = tmCache.containsKey(countryCode) && !tmCache.get(countryCode).isExpired();

        String requestId = null;
        TrustMaterialsResponse trustMaterials = null;
        ClientPaResult.TrustMaterialsInfo tmInfo = null;

        try {
            trustMaterials = tmFuture.get(15, TimeUnit.SECONDS);
            if (trustMaterials != null && trustMaterials.success()) {
                requestId = trustMaterials.requestId();
                log.info("Trust Materials received: requestId={}, topLevel={}, dataLevel={}, cacheWas={}",
                    requestId,
                    trustMaterials.topLevelRequestId(),
                    trustMaterials.data() != null ? trustMaterials.data().requestId() : "null",
                    cacheHit ? "HIT" : "MISS");
                var data = trustMaterials.data();
                tmInfo = new ClientPaResult.TrustMaterialsInfo(
                    data != null && data.csca() != null ? data.csca().size() : 0,
                    data != null && data.linkCert() != null ? data.linkCert().size() : 0,
                    data != null && data.crl() != null ? data.crl().size() : 0,
                    countryCode
                );
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
        // null = 성공, non-null 문자열 = 실패 원인
        // effectiveRequestIdRef: 감사용 requestId 별도 발급 시 갱신되어 ClientPaResult에 반영됨
        final String finalRequestId = requestId;
        final String finalStatus = overallStatus;
        final DocumentReadResponse finalResponse = response;
        final AtomicReference<String> effectiveRequestIdRef = new AtomicReference<>(requestId);

        CompletableFuture<String> reportFuture = CompletableFuture.supplyAsync(() -> {
            String reportRequestId = finalRequestId;

            // Trust Materials 다운로드 실패 또는 캐시 폴백으로 requestId가 없는 경우에도
            // 서버에 감사 정보(encryptedMrz 포함)를 남기기 위해 requestId를 별도 발급 시도
            if (reportRequestId == null) {
                log.info("No requestId from trust materials, fetching separately for audit reporting...");
                try {
                    TrustMaterialsResponse auditTm = downloadTrustMaterials(countryCode, parsedSOD);
                    if (auditTm != null && auditTm.success() && auditTm.requestId() != null) {
                        reportRequestId = auditTm.requestId();
                        effectiveRequestIdRef.set(reportRequestId);
                        log.info("Audit requestId obtained: {}", reportRequestId);
                    }
                } catch (Exception e2) {
                    log.warn("Failed to obtain audit requestId: {}", e2.getMessage());
                }
            }

            if (reportRequestId == null) {
                return "No requestId (trust materials unavailable and audit request failed)";
            }

            try {
                // 서버가 trust_material_request 레코드를 DB에 커밋하기 전에 결과를 보고하면
                // "requestId may not exist" 400 오류가 발생하는 race condition 방지
                Thread.sleep(300);
                reportResult(reportRequestId, finalStatus, sodSigResult, dgHashResult,
                    trustChainResult, crlCheckResult, (int) duration, finalResponse);
                log.info("Client PA result reported: requestId={}, status={}", reportRequestId, finalStatus);
                return null; // 성공
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Interrupted while waiting to report";
            } catch (Exception e) {
                log.warn("Failed to report client PA result: {}", e.getMessage());
                return e.getMessage(); // 실제 오류 메시지 반환
            }
        }, executor);

        // 보고 결과를 짧게 대기 (최대 6초: 감사용 requestId 발급 + 300ms delay + 네트워크 왕복)
        boolean serverReported = false;
        String serverReportError = null;
        try {
            String reportError = reportFuture.get(6, TimeUnit.SECONDS);
            if (reportError == null) {
                serverReported = true;
            } else {
                serverReportError = reportError;
            }
        } catch (TimeoutException e) {
            serverReported = true; // 백그라운드에서 계속 수행 중
            log.debug("Result report still in progress (background)");
        } catch (Exception e) {
            serverReportError = e.getMessage();
        }

        String effectiveRequestId = effectiveRequestIdRef.get();

        log.info("Client PA completed: status={}, duration={}ms, cache={}, requestId={}, " +
                "sodSig={}, dgHash={}/{}, trustChain={}, crl={}",
            overallStatus, duration, cacheHit ? "HIT" : "MISS", effectiveRequestId,
            sodSigResult.valid(),
            dgHashResult.validGroups(), dgHashResult.totalGroups(),
            trustChainResult.valid(), crlCheckResult.passed());

        return new ClientPaResult(
            "CLIENT", overallStatus, duration, effectiveRequestId,
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
            try {
                TrustMaterialsResponse fresh = downloadTrustMaterials(countryCode, parsedSOD);
                if (fresh != null && fresh.success()) {
                    tmCache.put(countryCode, new CachedTrustMaterials(fresh));
                    return fresh;
                }
            } catch (Exception e) {
                // fresh download 실패 시 cached CSCA/CRL 데이터는 유지하되
                // requestId는 null로 대체 — 만료된 requestId로 보고하면 서버가 400 반환
                log.warn("Fresh Trust Materials download failed for country={}: {}. " +
                    "Verification will proceed with cached data but result will NOT be reported.",
                    countryCode, e.getMessage());
                var d = cached.response.data();
                return new TrustMaterialsResponse(
                    true,
                    new TrustMaterialsResponse.TrustMaterialsData(
                        null,  // requestId 제거 → 결과 보고 스킵
                        d.countryCode(), d.csca(), d.linkCertificates(),
                        d.crl(), d.processingTimeMs(), d.timestamp()
                    ),
                    null
                );
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
            int processingTimeMs,
            DocumentReadResponse response) {

        String verificationMessage = buildVerificationMessage(overallStatus, sodSig, dgHash, trustChain, crlCheck);
        String encryptedMrz = buildEncryptedMrz(response);

        ClientPaReportRequest report = new ClientPaReportRequest(
            requestId, overallStatus, verificationMessage,
            trustChain.valid(), sodSig.valid(),
            dgHash.invalidGroups() == 0,
            crlCheck.checked() && crlCheck.passed(),
            processingTimeMs, encryptedMrz);

        log.info("Reporting Client PA result: requestId={}, status={}, trustChain={}, sodSig={}, dgHash={}, crl={}, encryptedMrz={}",
            requestId, overallStatus,
            trustChain.valid(), sodSig.valid(),
            dgHash.invalidGroups() == 0,
            crlCheck.checked() && crlCheck.passed(),
            encryptedMrz != null ? "present(" + encryptedMrz.length() + "chars)" : "null");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Connection", "close");
        HttpEntity<ClientPaReportRequest> entity = new HttpEntity<>(report, headers);

        ResponseEntity<Map> reportResponse = paApiRestTemplate.postForEntity(
            "/api/pa/trust-materials/result", entity, Map.class);
        log.info("Report response: status={}, body={}", reportResponse.getStatusCode(), reportResponse.getBody());
    }

    /**
     * 검증 결과 상세 메시지 생성
     */
    private String buildVerificationMessage(String overallStatus,
            ClientPaResult.SodSignatureResult sodSig,
            ClientPaResult.DgHashResult dgHash,
            ClientPaResult.TrustChainResult trustChain,
            ClientPaResult.CrlCheckResult crlCheck) {

        if ("VALID".equals(overallStatus)) {
            String chainPath = trustChain.chainPath() != null ? trustChain.chainPath() : "CSCA";
            return String.format(
                "Passive Authentication successful: SOD signature valid (%s), " +
                "DG hashes valid (%d/%d), trust chain verified via %s, CRL check passed.",
                sodSig.signatureAlgorithm() != null ? sodSig.signatureAlgorithm() : "OK",
                dgHash.validGroups(), dgHash.totalGroups(),
                chainPath);
        }

        List<String> failures = new ArrayList<>();
        if (!sodSig.valid()) {
            failures.add("SOD signature invalid" +
                (sodSig.errorMessage() != null ? ": " + sodSig.errorMessage() : ""));
        }
        if (dgHash.invalidGroups() > 0) {
            failures.add(dgHash.invalidGroups() + " DG hash(es) invalid");
        }
        if (crlCheck.revoked()) {
            failures.add("DSC certificate revoked");
        } else if (trustChain.available() && !trustChain.valid()) {
            failures.add("trust chain verification failed" +
                (trustChain.errorMessage() != null ? ": " + trustChain.errorMessage() : ""));
        }
        if (!trustChain.available()) {
            failures.add("trust materials unavailable");
        }

        if (failures.isEmpty()) {
            return "Verification " + overallStatus.toLowerCase() + ".";
        }
        return "Passive Authentication " + overallStatus + ": " + String.join("; ", failures) + ".";
    }

    /**
     * MRZ를 AES-256-GCM으로 암호화하여 Base64 반환
     * 암호화 키: SHA-256(apiKey) → 32바이트 AES-256 키
     * 출력 형식: Base64(IV[12] + Ciphertext + AuthTag[16])
     */
    private String buildEncryptedMrz(DocumentReadResponse response) {
        if (response == null) return null;

        // ePassMrzLines 우선, 없으면 mrzLines 사용
        com.smartcoreinc.fphps.dto.mrz.MrzLines mrzLines = response.getEPassMrzLines();
        if (mrzLines == null) mrzLines = response.getMrzLines();
        if (mrzLines == null || mrzLines.getLine1() == null || mrzLines.getLine2() == null) return null;

        String line1 = mrzLines.getLine1().trim();
        String line2 = mrzLines.getLine2().trim();
        if (line1.isEmpty() || line2.isEmpty()) return null;

        String apiKey = paApiSettingsService.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("API key not configured; skipping MRZ encryption");
            return null;
        }

        try {
            // AES-256 키 도출: SHA-256(apiKey UTF-8 bytes)
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha256.digest(apiKey.getBytes(StandardCharsets.UTF_8));

            // TD3 MRZ 평문: line1\nline2
            String mrzText = line1 + "\n" + line2;
            byte[] plaintext = mrzText.getBytes(StandardCharsets.UTF_8);

            // AES-256-GCM 암호화
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            byte[] iv = new byte[12];
            new SecureRandom().nextBytes(iv);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
            byte[] ciphertext = cipher.doFinal(plaintext);

            // 출력: IV(12) + Ciphertext+AuthTag
            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.warn("MRZ encryption failed: {}", e.getMessage());
            return null;
        }
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
