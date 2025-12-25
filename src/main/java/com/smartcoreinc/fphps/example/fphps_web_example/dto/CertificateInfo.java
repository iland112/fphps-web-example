package com.smartcoreinc.fphps.example.fphps_web_example.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * X509Certificate 정보를 UI 표시용으로 변환한 DTO
 */
@Getter
@Builder
@ToString
public class CertificateInfo implements Serializable {

    // 식별 정보
    private String subject;
    private String issuer;
    private String serialNumber;

    // 유효기간
    private String notBefore;
    private String notAfter;
    private boolean valid;

    // 공개키 정보
    private String publicKeyAlgorithm;
    private int publicKeySize;
    private String publicKeyFormat;

    // 서명 정보
    private String signatureAlgorithm;
    private String signatureAlgorithmOID;

    // 지문 (Fingerprint)
    private String sha1Fingerprint;
    private String sha256Fingerprint;

    // 버전 및 기타
    private int version;
    private String type;

    // 확장 필드 목록
    private List<ExtensionInfo> extensions;

    /**
     * X509Certificate로부터 CertificateInfo 생성
     */
    public static CertificateInfo from(X509Certificate cert) {
        if (cert == null) {
            return null;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();

        return CertificateInfo.builder()
                .subject(cert.getSubjectX500Principal().getName())
                .issuer(cert.getIssuerX500Principal().getName())
                .serialNumber(cert.getSerialNumber().toString(16).toUpperCase())
                .notBefore(formatDate(cert.getNotBefore(), formatter))
                .notAfter(formatDate(cert.getNotAfter(), formatter))
                .valid(now.after(cert.getNotBefore()) && now.before(cert.getNotAfter()))
                .publicKeyAlgorithm(cert.getPublicKey().getAlgorithm())
                .publicKeySize(getKeySize(cert))
                .publicKeyFormat(cert.getPublicKey().getFormat())
                .signatureAlgorithm(cert.getSigAlgName())
                .signatureAlgorithmOID(cert.getSigAlgOID())
                .sha1Fingerprint(getFingerprint(cert, "SHA-1"))
                .sha256Fingerprint(getFingerprint(cert, "SHA-256"))
                .version(cert.getVersion())
                .type(cert.getType())
                .extensions(extractExtensions(cert))
                .build();
    }

    /**
     * Date를 포맷팅된 문자열로 변환
     */
    private static String formatDate(Date date, DateTimeFormatter formatter) {
        if (date == null) return "";
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .format(formatter);
    }

    /**
     * 공개키 크기 추출 (RSA, DSA, EC 등)
     */
    private static int getKeySize(X509Certificate cert) {
        try {
            String algorithm = cert.getPublicKey().getAlgorithm();
            if ("RSA".equals(algorithm) || "DSA".equals(algorithm)) {
                // RSA/DSA의 경우 모듈러스 크기
                java.security.interfaces.RSAPublicKey rsaKey =
                    (java.security.interfaces.RSAPublicKey) cert.getPublicKey();
                return rsaKey.getModulus().bitLength();
            } else if ("EC".equals(algorithm)) {
                // EC의 경우 필드 크기
                java.security.interfaces.ECPublicKey ecKey =
                    (java.security.interfaces.ECPublicKey) cert.getPublicKey();
                return ecKey.getParams().getCurve().getField().getFieldSize();
            }
        } catch (Exception e) {
            // 타입 캐스팅 실패 시 0 반환
        }
        return 0;
    }

    /**
     * 인증서 지문(Fingerprint) 생성
     */
    private static String getFingerprint(X509Certificate cert, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] der = cert.getEncoded();
            byte[] digest = md.digest(der);
            return bytesToHex(digest, ":");
        } catch (NoSuchAlgorithmException | CertificateEncodingException e) {
            return "N/A";
        }
    }

    /**
     * 바이트 배열을 16진수 문자열로 변환 (구분자 포함)
     */
    private static String bytesToHex(byte[] bytes, String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0) sb.append(separator);
            sb.append(String.format("%02X", bytes[i]));
        }
        return sb.toString();
    }

    /**
     * X509 확장 필드 추출
     */
    private static List<ExtensionInfo> extractExtensions(X509Certificate cert) {
        List<ExtensionInfo> extensions = new ArrayList<>();

        if (cert.getCriticalExtensionOIDs() != null) {
            for (String oid : cert.getCriticalExtensionOIDs()) {
                extensions.add(ExtensionInfo.builder()
                        .oid(oid)
                        .name(getExtensionName(oid))
                        .critical(true)
                        .value(bytesToHex(cert.getExtensionValue(oid), " "))
                        .build());
            }
        }

        if (cert.getNonCriticalExtensionOIDs() != null) {
            for (String oid : cert.getNonCriticalExtensionOIDs()) {
                extensions.add(ExtensionInfo.builder()
                        .oid(oid)
                        .name(getExtensionName(oid))
                        .critical(false)
                        .value(bytesToHex(cert.getExtensionValue(oid), " "))
                        .build());
            }
        }

        return extensions;
    }

    /**
     * OID를 확장 필드 이름으로 변환
     */
    private static String getExtensionName(String oid) {
        return switch (oid) {
            case "2.5.29.14" -> "Subject Key Identifier";
            case "2.5.29.15" -> "Key Usage";
            case "2.5.29.16" -> "Private Key Usage Period";
            case "2.5.29.17" -> "Subject Alternative Name";
            case "2.5.29.18" -> "Issuer Alternative Name";
            case "2.5.29.19" -> "Basic Constraints";
            case "2.5.29.30" -> "Name Constraints";
            case "2.5.29.31" -> "CRL Distribution Points";
            case "2.5.29.32" -> "Certificate Policies";
            case "2.5.29.33" -> "Policy Mappings";
            case "2.5.29.35" -> "Authority Key Identifier";
            case "2.5.29.36" -> "Policy Constraints";
            case "2.5.29.37" -> "Extended Key Usage";
            case "1.3.6.1.5.5.7.1.1" -> "Authority Information Access";
            default -> "Unknown (" + oid + ")";
        };
    }

    /**
     * 확장 필드 정보 내부 클래스
     */
    @Getter
    @Builder
    @ToString
    public static class ExtensionInfo implements Serializable {
        private String oid;
        private String name;
        private boolean critical;
        private String value;
    }
}
