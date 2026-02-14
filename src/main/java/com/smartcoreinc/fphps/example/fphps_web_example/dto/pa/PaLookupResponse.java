package com.smartcoreinc.fphps.example.fphps_web_example.dto.pa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * PA Lookup API 응답 래퍼 DTO
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PaLookupResponse(
    boolean success,
    PaLookupValidation validation,
    String message
) {}
