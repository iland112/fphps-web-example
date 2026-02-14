package com.smartcoreinc.fphps.example.fphps_web_example.dto.pa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Data Group 상세 검증 결과
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DataGroupDetail(
    boolean valid,
    String expectedHash,
    String actualHash
) {}
