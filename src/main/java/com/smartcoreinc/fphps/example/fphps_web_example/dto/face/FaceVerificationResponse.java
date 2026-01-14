package com.smartcoreinc.fphps.example.fphps_web_example.dto.face;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Face verification response DTO
 * 
 * @param status Overall status: MATCHED, NOT_MATCHED, ERROR
 * @param verificationId Unique verification ID (UUID)
 * @param matchScore Cosine similarity score (0-1)
 * @param matchStatus Match result: MATCH, NO_MATCH, UNCERTAIN, ERROR
 * @param confidenceLevel Confidence level: HIGH, MEDIUM, LOW
 * @param threshold Threshold used for matching
 * @param documentPhotoQuality Document photo quality metrics
 * @param chipPhotoQuality Chip photo quality metrics
 * @param processingDurationMs Processing time in milliseconds
 * @param errors Error messages if any
 */
public record FaceVerificationResponse(
    String status,
    @JsonProperty("verification_id") String verificationId,
    @JsonProperty("match_score") Double matchScore,
    @JsonProperty("match_status") String matchStatus,
    @JsonProperty("confidence_level") String confidenceLevel,
    Double threshold,
    @JsonProperty("document_photo_quality") FaceQualityMetrics documentPhotoQuality,
    @JsonProperty("chip_photo_quality") FaceQualityMetrics chipPhotoQuality,
    @JsonProperty("processing_duration_ms") Long processingDurationMs,
    List<String> errors
) {
    /**
     * Check if faces are matched
     * 
     * @return true if status is MATCHED
     */
    public boolean isMatched() {
        return "MATCHED".equals(status);
    }
    
    /**
     * Check if verification has error
     * 
     * @return true if status is ERROR
     */
    public boolean hasError() {
        return "ERROR".equals(status);
    }
    
    /**
     * Check if confidence is high
     * 
     * @return true if confidence level is HIGH
     */
    public boolean isHighConfidence() {
        return "HIGH".equals(confidenceLevel);
    }
}
