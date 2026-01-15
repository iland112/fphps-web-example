package com.smartcoreinc.fphps.example.fphps_web_example.dto.face;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Face bounding box coordinates
 *
 * @param x1 Top-left X coordinate
 * @param y1 Top-left Y coordinate
 * @param x2 Bottom-right X coordinate
 * @param y2 Bottom-right Y coordinate
 */
public record BoundingBox(
    @JsonProperty("x1") Integer x1,
    @JsonProperty("y1") Integer y1,
    @JsonProperty("x2") Integer x2,
    @JsonProperty("y2") Integer y2
) {}
