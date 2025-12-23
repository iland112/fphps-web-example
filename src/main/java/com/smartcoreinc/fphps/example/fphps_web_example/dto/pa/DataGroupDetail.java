package com.smartcoreinc.fphps.example.fphps_web_example.dto.pa;

/**
 * Data Group 상세 검증 결과
 */
public record DataGroupDetail(
    boolean valid,
    String expectedHash,
    String actualHash
) {}
