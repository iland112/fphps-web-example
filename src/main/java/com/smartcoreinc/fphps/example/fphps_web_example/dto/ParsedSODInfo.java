package com.smartcoreinc.fphps.example.fphps_web_example.dto;

import com.smartcoreinc.fphps.sod.ParsedSOD;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * ParsedSOD 정보를 UI 표시용으로 변환한 DTO
 */
@Getter
@Builder
@ToString
public class ParsedSODInfo implements Serializable {

    // DSC 인증서 정보
    private CertificateInfo dscCertificate;

    // Data Group 해시 목록
    private List<DataGroupHash> dataGroupHashes;

    // 다이제스트 알고리즘 정보
    private String digestAlgorithmOid;
    private String digestAlgorithmName;

    // 서명자 정보
    private SignerInfo signerInfo;

    /**
     * ParsedSOD로부터 ParsedSODInfo 생성
     */
    public static ParsedSODInfo from(ParsedSOD parsedSOD) {
        if (parsedSOD == null) {
            return null;
        }

        return ParsedSODInfo.builder()
                .dscCertificate(CertificateInfo.from(parsedSOD.dscCertificate()))
                .dataGroupHashes(extractDataGroupHashes(parsedSOD.dgHashes()))
                .digestAlgorithmOid(parsedSOD.digestAlgorithmOid())
                .digestAlgorithmName(getAlgorithmName(parsedSOD.digestAlgorithmOid()))
                .signerInfo(extractSignerInfo(parsedSOD))
                .build();
    }

    /**
     * Data Group 해시 맵을 리스트로 변환
     */
    private static List<DataGroupHash> extractDataGroupHashes(Map<Integer, byte[]> dgHashes) {
        List<DataGroupHash> hashList = new ArrayList<>();

        if (dgHashes == null || dgHashes.isEmpty()) {
            return hashList;
        }

        // TreeMap으로 정렬 (DG01, DG02, ... 순서)
        TreeMap<Integer, byte[]> sortedMap = new TreeMap<>(dgHashes);

        for (Map.Entry<Integer, byte[]> entry : sortedMap.entrySet()) {
            hashList.add(DataGroupHash.builder()
                    .dgNumber(entry.getKey())
                    .dgName(String.format("DG%02d", entry.getKey()))
                    .hashValue(bytesToHex(entry.getValue()))
                    .hashBytes(entry.getValue())
                    .build());
        }

        return hashList;
    }

    /**
     * OID로부터 알고리즘 이름 추출
     */
    private static String getAlgorithmName(String oid) {
        if (oid == null) return "Unknown";

        return switch (oid) {
            case "1.3.14.3.2.26" -> "SHA-1";
            case "2.16.840.1.101.3.4.2.1" -> "SHA-256";
            case "2.16.840.1.101.3.4.2.2" -> "SHA-384";
            case "2.16.840.1.101.3.4.2.3" -> "SHA-512";
            case "2.16.840.1.101.3.4.2.4" -> "SHA-224";
            case "1.2.840.113549.1.1.5" -> "SHA1withRSA";
            case "1.2.840.113549.1.1.11" -> "SHA256withRSA";
            case "1.2.840.113549.1.1.12" -> "SHA384withRSA";
            case "1.2.840.113549.1.1.13" -> "SHA512withRSA";
            default -> "Unknown (" + oid + ")";
        };
    }

    /**
     * SignerInformation으로부터 서명자 정보 추출
     */
    private static SignerInfo extractSignerInfo(ParsedSOD parsedSOD) {
        if (parsedSOD.signerInformation() == null) {
            return null;
        }

        try {
            var signerInfo = parsedSOD.signerInformation();

            return SignerInfo.builder()
                    .digestAlgorithm(signerInfo.getDigestAlgOID())
                    .encryptionAlgorithm(signerInfo.getEncryptionAlgOID())
                    .signature(bytesToHex(signerInfo.getSignature()))
                    .build();
        } catch (Exception e) {
            return SignerInfo.builder()
                    .digestAlgorithm("N/A")
                    .encryptionAlgorithm("N/A")
                    .signature("Error extracting signature")
                    .build();
        }
    }

    /**
     * 바이트 배열을 16진수 문자열로 변환
     */
    private static String bytesToHex(byte[] bytes) {
        if (bytes == null) return "";

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    /**
     * Data Group 해시 정보
     */
    @Getter
    @Builder
    @ToString
    public static class DataGroupHash implements Serializable {
        private int dgNumber;          // DG 번호 (1-16)
        private String dgName;         // DG 이름 (DG01, DG02, ...)
        private String hashValue;      // 16진수 해시값
        private byte[] hashBytes;      // 원본 바이트 배열
    }

    /**
     * 서명자 정보
     */
    @Getter
    @Builder
    @ToString
    public static class SignerInfo implements Serializable {
        private String digestAlgorithm;
        private String encryptionAlgorithm;
        private String signature;
    }
}
