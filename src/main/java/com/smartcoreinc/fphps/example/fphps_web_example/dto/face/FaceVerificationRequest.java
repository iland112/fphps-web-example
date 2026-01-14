package com.smartcoreinc.fphps.example.fphps_web_example.dto.face;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Face verification request DTO
 * 
 * @param documentPhoto Base64 encoded PNG image from scanner
 * @param chipPhoto Base64 encoded PNG image from chip (DG2)
 * @param country Issuing country code
 * @param documentNumber Passport number
 * @param requestedBy Client identifier
 */
public record FaceVerificationRequest(
    @JsonProperty("document_photo") String documentPhoto,
    @JsonProperty("chip_photo") String chipPhoto,
    String country,
    @JsonProperty("document_number") String documentNumber,
    @JsonProperty("requested_by") String requestedBy
) {
    /**
     * Factory method to create request from Base64 images
     * 
     * @param docPhotoBase64 Document photo Base64 string
     * @param chipPhotoBase64 Chip photo Base64 string
     * @param country Issuing country
     * @param docNumber Document number
     * @param requestedBy Client ID
     * @return FaceVerificationRequest instance
     */
    public static FaceVerificationRequest of(
        String docPhotoBase64,
        String chipPhotoBase64,
        String country,
        String docNumber,
        String requestedBy
    ) {
        return new FaceVerificationRequest(
            docPhotoBase64,
            chipPhotoBase64,
            country,
            docNumber,
            requestedBy
        );
    }
}
