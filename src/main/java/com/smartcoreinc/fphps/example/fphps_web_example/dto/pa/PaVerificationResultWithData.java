package com.smartcoreinc.fphps.example.fphps_web_example.dto.pa;

import com.smartcoreinc.fphps.dto.DocumentReadResponse;
import com.smartcoreinc.fphps.dto.mrz.MrzInfo;
import com.smartcoreinc.fphps.dto.mrz.MrzLines;

/**
 * PA 검증 결과와 함께 MRZ 및 Face 이미지 데이터를 포함하는 응답 DTO
 */
public record PaVerificationResultWithData(
    // PA 검증 결과
    PaVerificationResponse paResult,

    // DG1: MRZ 데이터
    MrzData mrzData,

    // DG2: Face 이미지 (Base64 data URI)
    String faceImageBase64
) {

    /**
     * DocumentReadResponse에서 PA 결과와 함께 MRZ/Face 데이터 추출
     */
    public static PaVerificationResultWithData from(
            PaVerificationResponse paResult,
            DocumentReadResponse docResponse) {

        MrzData mrzData = null;
        String faceImageBase64 = null;

        if (docResponse != null) {
            // MRZ 데이터 추출
            MrzInfo mrzInfo = docResponse.getMrzInfo();
            MrzLines mrzLines = docResponse.getMrzLines();
            if (mrzInfo != null) {
                mrzData = MrzData.from(mrzInfo, mrzLines);
            }

            // Face 이미지 추출 (이미 Base64 인코딩된 imageData 사용)
            if (docResponse.getEPassPhotoImage() != null
                && docResponse.getEPassPhotoImage().getImageData() != null
                && !docResponse.getEPassPhotoImage().getImageData().isEmpty()) {
                faceImageBase64 = "data:image/png;base64," + docResponse.getEPassPhotoImage().getImageData();
            }
        }

        return new PaVerificationResultWithData(paResult, mrzData, faceImageBase64);
    }

    /**
     * MRZ 데이터 DTO
     */
    public record MrzData(
        String documentType,
        String issuingState,
        String name,
        String passportNumber,
        String nationality,
        String dateOfBirth,
        String sex,
        String expiryDate,
        String optionalData,
        String mrzLine1,
        String mrzLine2
    ) {
        public static MrzData from(MrzInfo mrzInfo, MrzLines mrzLines) {
            if (mrzInfo == null) return null;

            String line1 = null;
            String line2 = null;
            if (mrzLines != null) {
                line1 = mrzLines.getLine1();
                line2 = mrzLines.getLine2();
            }

            return new MrzData(
                mrzInfo.getDocType(),
                mrzInfo.getIssuingState(),
                mrzInfo.getName(),
                mrzInfo.getPassportNumber(),
                mrzInfo.getNationality(),
                mrzInfo.getBirth(),
                mrzInfo.getSex(),
                mrzInfo.getExpiryDate(),
                mrzInfo.getOpt(),
                line1,
                line2
            );
        }
    }
}
