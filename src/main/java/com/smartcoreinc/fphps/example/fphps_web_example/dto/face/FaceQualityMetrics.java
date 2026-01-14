package com.smartcoreinc.fphps.example.fphps_web_example.dto.face;

/**
 * Face quality metrics
 * 
 * @param detectionScore Face detection confidence score (0-1)
 * @param faceAreaRatio Ratio of face area to image area (0-1)
 * @param brightnessScore Image brightness quality (0-1)
 * @param sharpnessScore Image sharpness quality (0-1)
 * @param poseScore Face pose quality (0-1, frontal=1)
 */
public record FaceQualityMetrics(
    Double detectionScore,
    Double faceAreaRatio,
    Double brightnessScore,
    Double sharpnessScore,
    Double poseScore
) {}
