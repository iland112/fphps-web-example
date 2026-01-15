package com.smartcoreinc.fphps.example.fphps_web_example.dto.face;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Face quality metrics
 *
 * @param detectionScore Face detection confidence score (0-1)
 * @param faceAreaRatio Ratio of face area to image area (0-1)
 * @param brightnessScore Image brightness quality (0-1)
 * @param sharpnessScore Image sharpness quality (0-1)
 * @param poseScore Face pose quality (0-1, frontal=1)
 * @param imageBase64 Base64 encoded original image
 * @param bbox Face bounding box coordinates
 */
public record FaceQualityMetrics(
    @JsonProperty("detection_score") Double detectionScore,
    @JsonProperty("face_area_ratio") Double faceAreaRatio,
    @JsonProperty("brightness_score") Double brightnessScore,
    @JsonProperty("sharpness_score") Double sharpnessScore,
    @JsonProperty("pose_score") Double poseScore,
    @JsonProperty("image_base64") String imageBase64,
    @JsonProperty("bbox") BoundingBox bbox
) {}
