package com.smartcoreinc.fphps.example.fphps_web_example.dto.pa;

import java.time.LocalDateTime;

/**
 * PA 검증 에러 정보
 */
public record PaError(
    String code,
    String message,
    String severity,
    LocalDateTime timestamp
) {}
