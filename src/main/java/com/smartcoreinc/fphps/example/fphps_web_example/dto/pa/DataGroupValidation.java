package com.smartcoreinc.fphps.example.fphps_web_example.dto.pa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/**
 * Data Group 해시 검증 결과
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DataGroupValidation(
    int totalGroups,
    int validGroups,
    int invalidGroups,
    Map<String, DataGroupDetail> details
) {}
