package com.smartcoreinc.fphps.example.fphps_web_example.Services;

import com.smartcoreinc.fphps.dto.DocumentReadResponse;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.face.FaceVerificationRequest;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.face.FaceVerificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class FaceVerificationService {
    
    private final RestTemplate faceApiRestTemplate;
    
    public FaceVerificationService(
        @Qualifier("faceApiRestTemplate") RestTemplate faceApiRestTemplate
    ) {
        this.faceApiRestTemplate = faceApiRestTemplate;
    }
    
    public FaceVerificationResponse verify(
        String docPhotoBase64,
        String chipPhotoBase64,
        String country,
        String documentNumber
    ) {
        FaceVerificationRequest request = FaceVerificationRequest.of(
            docPhotoBase64,
            chipPhotoBase64,
            country,
            documentNumber,
            "fphps-web-example"
        );
        
        try {
            log.info("Sending face verification request for country={}, docNumber={}",
                country, documentNumber);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Connection", "close");
            headers.set("Content-Type", "application/json");
            HttpEntity<FaceVerificationRequest> requestEntity = 
                new HttpEntity<>(request, headers);
            
            ResponseEntity<FaceVerificationResponse> response = 
                faceApiRestTemplate.postForEntity(
                    "/api/face/verify",
                    requestEntity,
                    FaceVerificationResponse.class
                );
            
            FaceVerificationResponse result = response.getBody();
            
            if (result != null) {
                log.info("Face verification completed: status={}, matchScore={}, confidence={}",
                    result.status(), result.matchScore(), result.confidenceLevel());
            }
            
            return result;
            
        } catch (HttpClientErrorException e) {
            log.error("Face verification client error: {}", e.getResponseBodyAsString());
            throw new FaceVerificationException(
                "Face verification request failed: " + e.getMessage(), e);
        } catch (HttpServerErrorException e) {
            log.error("Face verification server error: {}", e.getResponseBodyAsString());
            throw new FaceVerificationException(
                "Face API server error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during face verification: {}", e.getMessage(), e);
            throw new FaceVerificationException(
                "Unexpected error: " + e.getMessage(), e);
        }
    }
    
    public FaceVerificationResponse verifyFromDocumentResponse(
        DocumentReadResponse response
    ) {
        if (response == null) {
            throw new FaceVerificationException("DocumentReadResponse is null");
        }
        
        String docPhotoBase64 = null;
        String chipPhotoBase64 = null;
        
        try {
            if (response.getVizPhotoImage() != null) {
                docPhotoBase64 = response.getVizPhotoImage().getImageData();
            }
            
            if (response.getEPassPhotoImage() != null) {
                chipPhotoBase64 = response.getEPassPhotoImage().getImageData();
            }
        } catch (Exception e) {
            log.error("Error extracting images from response: {}", e.getMessage(), e);
            throw new FaceVerificationException(
                "Failed to extract images: " + e.getMessage(), e);
        }
        
        if (docPhotoBase64 == null || docPhotoBase64.isEmpty()) {
            throw new FaceVerificationException("Document photo image is missing or empty");
        }
        
        if (chipPhotoBase64 == null || chipPhotoBase64.isEmpty()) {
            throw new FaceVerificationException("Chip photo image is missing or empty");
        }
        
        String country = extractIssuingCountry(response);
        String documentNumber = extractDocumentNumber(response);
        
        return verify(docPhotoBase64, chipPhotoBase64, country, documentNumber);
    }
    
    private String extractIssuingCountry(DocumentReadResponse response) {
        try {
            if (response.getMrzInfo() != null && 
                response.getMrzInfo().getIssuingState() != null) {
                return response.getMrzInfo().getIssuingState();
            }
        } catch (Exception e) {
            log.warn("Error extracting issuing country: {}", e.getMessage());
        }
        return "UNKNOWN";
    }
    
    private String extractDocumentNumber(DocumentReadResponse response) {
        try {
            if (response.getMrzInfo() != null && 
                response.getMrzInfo().getPassportNumber() != null) {
                return response.getMrzInfo().getPassportNumber();
            }
        } catch (Exception e) {
            log.warn("Error extracting document number: {}", e.getMessage());
        }
        return "UNKNOWN";
    }
    
    public static class FaceVerificationException extends RuntimeException {
        public FaceVerificationException(String message) {
            super(message);
        }
        
        public FaceVerificationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
