package com.smartcoreinc.fphps.example.fphps_web_example.dto.pa;

/**
 * 클라이언트 PA Step 1: Trust Materials 요청 DTO
 * POST /api/pa/trust-materials
 */
public record TrustMaterialsRequest(
    String countryCode,
    String dscIssuerDn,
    String requestedBy
) {}
