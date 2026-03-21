package com.smartcoreinc.fphps.example.fphps_web_example.Services;

import com.smartcoreinc.fphps.dto.DocumentReadResponse;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.CertificateInfo;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.ParsedSODInfo;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.pa.ClientPaResult;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.pa.PaLookupRequest;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.pa.PaLookupResponse;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.pa.PaLookupValidation;
import com.smartcoreinc.fphps.sod.DataGroupHashVerifier;
import com.smartcoreinc.fphps.sod.ParsedSOD;
import com.smartcoreinc.fphps.sod.SODSignatureVerifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 클라이언트 모드 PA 검증 서비스
 *
 * 로컬에서 SOD 서명 검증 + DG 해시 검증을 수행하고,
 * Trust Chain만 PA Lookup API로 조회하여 종합 결과를 반환
 */
@Slf4j
@Service
public class ClientPaVerificationService {

    private static final DateTimeFormatter CERT_DATE_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestTemplate paApiRestTemplate;

    public ClientPaVerificationService(RestTemplate paApiRestTemplate) {
        this.paApiRestTemplate = paApiRestTemplate;
    }

    /**
     * 클라이언트 모드 PA 검증 수행
     *
     * 1. SOD 서명 검증 (로컬, Bouncy Castle)
     * 2. DG 해시 검증 (로컬)
     * 3. Trust Chain 조회 (PA Lookup API)
     * 4. 종합 결과 조립
     */
    public ClientPaResult verify(DocumentReadResponse response) {
        long startTime = System.currentTimeMillis();
        List<String> errors = new ArrayList<>();

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

        // ── Step 1: SOD 서명 검증 (로컬) ──
        ClientPaResult.SodSignatureResult sodSigResult = verifySodSignature(parsedSOD, errors);

        // ── Step 2: DG 해시 검증 (로컬) ──
        ClientPaResult.DgHashResult dgHashResult = verifyDgHashes(parsedSOD, dgDataMap, errors);

        // ── Step 3: DSC 인증서 정보 추출 (로컬) ──
        ClientPaResult.DscInfo dscInfo = extractDscInfo(parsedSOD);

        // ── Step 4: Trust Chain 조회 (PA Lookup API) ──
        PaLookupValidation trustChainLookup = null;
        boolean trustChainAvailable = false;
        try {
            PaLookupResponse lookupResponse = callPaLookup(parsedSOD);
            if (lookupResponse != null && lookupResponse.success()) {
                trustChainLookup = lookupResponse.validation();
                trustChainAvailable = true;
            }
        } catch (Exception e) {
            log.warn("Trust Chain lookup failed (PA API unavailable): {}", e.getMessage());
            errors.add("Trust Chain lookup failed: " + e.getMessage());
        }

        // ── Step 5: 종합 상태 결정 ──
        String overallStatus = determineOverallStatus(
            sodSigResult, dgHashResult, trustChainLookup, trustChainAvailable);

        long duration = System.currentTimeMillis() - startTime;

        log.info("Client PA verification completed: status={}, duration={}ms, " +
                "sodSig={}, dgHash={}/{}, trustChain={}",
            overallStatus, duration,
            sodSigResult.valid(),
            dgHashResult.validGroups(), dgHashResult.totalGroups(),
            trustChainAvailable ? (trustChainLookup != null ?
                trustChainLookup.validationStatus() : "NOT_FOUND") : "UNAVAILABLE");

        return new ClientPaResult(
            "CLIENT",
            overallStatus,
            duration,
            sodSigResult,
            dgHashResult,
            trustChainLookup,
            trustChainAvailable,
            dscInfo,
            errors.isEmpty() ? null : errors
        );
    }

    /**
     * SOD 서명 검증 (로컬)
     */
    private ClientPaResult.SodSignatureResult verifySodSignature(
            ParsedSOD parsedSOD, List<String> errors) {
        String hashAlg = ParsedSODInfo.from(parsedSOD).getDigestAlgorithmName();
        String sigAlg = parsedSOD.dscCertificate() != null
            ? parsedSOD.dscCertificate().getSigAlgName() : "Unknown";

        try {
            SODSignatureVerifier.verifySignature(
                parsedSOD.signerInformation(),
                parsedSOD.dscCertificate()
            );
            return new ClientPaResult.SodSignatureResult(true, hashAlg, sigAlg, null);
        } catch (Exception e) {
            String errorMsg = "SOD signature verification failed: " + e.getMessage();
            log.warn(errorMsg);
            errors.add(errorMsg);
            return new ClientPaResult.SodSignatureResult(false, hashAlg, sigAlg, e.getMessage());
        }
    }

    /**
     * DG 해시 검증 (로컬)
     */
    private ClientPaResult.DgHashResult verifyDgHashes(
            ParsedSOD parsedSOD,
            Map<Integer, byte[]> dgDataMap,
            List<String> errors) {

        Map<Integer, byte[]> sodHashes = parsedSOD.dgHashes();
        String digestAlgOid = parsedSOD.digestAlgorithmOid();
        String digestAlgName = ParsedSODInfo.from(parsedSOD).getDigestAlgorithmName();

        Map<String, ClientPaResult.DgHashDetail> details = new LinkedHashMap<>();
        int validCount = 0;
        int invalidCount = 0;

        // SOD에 포함된 DG 해시 기준으로 검증
        for (Map.Entry<Integer, byte[]> entry : new TreeMap<>(sodHashes).entrySet()) {
            int dgNum = entry.getKey();
            byte[] expectedHash = entry.getValue();
            String dgName = "DG" + dgNum;

            byte[] dgData = dgDataMap.get(dgNum);
            if (dgData == null) {
                // DG 데이터가 없는 경우 (SOD에는 해시가 있으나 읽지 않은 DG)
                details.put(dgName, new ClientPaResult.DgHashDetail(
                    false, bytesToHex(expectedHash), "N/A (not read)"));
                invalidCount++;
                continue;
            }

            try {
                // 실제 해시 계산
                String jcaAlgName = oidToJcaName(digestAlgOid);
                MessageDigest md = MessageDigest.getInstance(jcaAlgName);
                byte[] actualHash = md.digest(dgData);

                boolean match = MessageDigest.isEqual(expectedHash, actualHash);
                details.put(dgName, new ClientPaResult.DgHashDetail(
                    match, bytesToHex(expectedHash), bytesToHex(actualHash)));

                if (match) {
                    validCount++;
                } else {
                    invalidCount++;
                    errors.add("DG" + dgNum + " hash mismatch");
                }
            } catch (Exception e) {
                details.put(dgName, new ClientPaResult.DgHashDetail(
                    false, bytesToHex(expectedHash), "Error: " + e.getMessage()));
                invalidCount++;
                errors.add("DG" + dgNum + " hash calculation failed: " + e.getMessage());
            }
        }

        return new ClientPaResult.DgHashResult(
            validCount + invalidCount, validCount, invalidCount, details);
    }

    /**
     * DSC 인증서 정보 추출 (로컬)
     */
    private ClientPaResult.DscInfo extractDscInfo(ParsedSOD parsedSOD) {
        X509Certificate cert = parsedSOD.dscCertificate();
        if (cert == null) {
            return null;
        }

        CertificateInfo certInfo = CertificateInfo.from(cert);

        String sha256 = null;
        if (certInfo.getSha256Fingerprint() != null
                && !"N/A".equals(certInfo.getSha256Fingerprint())) {
            sha256 = certInfo.getSha256Fingerprint().replace(":", "").toLowerCase();
        }

        boolean expired = cert.getNotAfter().before(new Date());

        return new ClientPaResult.DscInfo(
            certInfo.getSubject(),
            certInfo.getIssuer(),
            certInfo.getSerialNumber(),
            certInfo.getNotBefore(),
            certInfo.getNotAfter(),
            expired,
            certInfo.getSignatureAlgorithm(),
            certInfo.getPublicKeyAlgorithm(),
            certInfo.getPublicKeySize(),
            sha256
        );
    }

    /**
     * PA Lookup API 호출하여 Trust Chain 조회
     */
    private PaLookupResponse callPaLookup(ParsedSOD parsedSOD) {
        CertificateInfo dscCert = CertificateInfo.from(parsedSOD.dscCertificate());

        String subjectDn = dscCert.getSubject();
        String fingerprint = null;
        if (dscCert.getSha256Fingerprint() != null
                && !"N/A".equals(dscCert.getSha256Fingerprint())) {
            fingerprint = dscCert.getSha256Fingerprint().replace(":", "").toLowerCase();
        }

        PaLookupRequest request = new PaLookupRequest(subjectDn, fingerprint);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Connection", "close");
        HttpEntity<PaLookupRequest> requestEntity = new HttpEntity<>(request, headers);

        ResponseEntity<PaLookupResponse> result = paApiRestTemplate.postForEntity(
            "/api/certificates/pa-lookup",
            requestEntity,
            PaLookupResponse.class
        );

        return result.getBody();
    }

    /**
     * 종합 상태 결정
     */
    private String determineOverallStatus(
            ClientPaResult.SodSignatureResult sodSig,
            ClientPaResult.DgHashResult dgHash,
            PaLookupValidation trustChain,
            boolean trustChainAvailable) {

        // SOD 서명 또는 DG 해시 실패 → INVALID
        if (!sodSig.valid() || dgHash.invalidGroups() > 0) {
            return "INVALID";
        }

        // Trust Chain 조회 불가 → PARTIAL
        if (!trustChainAvailable) {
            return "PARTIAL";
        }

        // Trust Chain 결과 없음 (DSC 미등록) → PARTIAL
        if (trustChain == null) {
            return "PARTIAL";
        }

        // Trust Chain 결과에 따라 VALID/INVALID
        if (trustChain.trustChainValid()) {
            return "VALID";
        }

        // Trust Chain이 유효하지 않지만 validationStatus가 EXPIRED_VALID인 경우
        if ("EXPIRED_VALID".equals(trustChain.validationStatus())) {
            return "EXPIRED_VALID";
        }

        return "INVALID";
    }

    /**
     * Digest Algorithm OID → JCA 알고리즘 이름 변환
     */
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
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
