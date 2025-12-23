package com.smartcoreinc.fphps.example.fphps_web_example.dto.pa;

import java.util.Map;

/**
 * Data Group 해시 검증 결과
 */
public record DataGroupValidation(
    int totalGroups,
    int validGroups,
    int invalidGroups,
    Map<String, DataGroupDetail> details
) {}
