package com.smartcoreinc.fphps.example.fphps_web_example.dto.pa;

/**
 * PA Lookup 요청 DTO
 * DSC Subject DN 또는 SHA-256 Fingerprint로 Trust Chain 조회
 */
public record PaLookupRequest(String subjectDn, String fingerprint) {}
