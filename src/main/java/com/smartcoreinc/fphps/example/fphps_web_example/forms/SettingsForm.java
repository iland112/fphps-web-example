package com.smartcoreinc.fphps.example.fphps_web_example.forms;

import java.io.Serializable;

import com.smartcoreinc.fphps.dto.properties.BatchModeProperties;
import com.smartcoreinc.fphps.dto.properties.EPassportAuthProperties;
import com.smartcoreinc.fphps.dto.properties.EPassportDGProperties;
import com.smartcoreinc.fphps.dto.properties.FPHPSDeviceProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Getter
@Setter
@Builder
public class SettingsForm implements Serializable {
    // private int readMethod;   // 0 -> One Step(DETECT+OCR+RF), 1 -> Two Step(DETECT => OCR+RF), 2 -> Three Step (DETECT => OCR => RF)
    private boolean checkRemove;        // 이전 판독 여권 존재 검사
    private boolean enableRF;
    // private boolean enableBuzzer;
    private int rfReadSize;             // 전자여권 읽기 크기 설정. 기본 231, 최대 4096
    // private int readTimeout;            // FPHPS_PROPERTY_TYPE.FPHPS_PT_READ_TIMEOUT 판독 타임아웃
    // private int detectDelayTime;        // FPHPS_PROPERTY_TYPE.FPHPS_PT_DETECT_DELAY_TIME 인식 후 판독 지연시간
    // private int userModeLed;
    private boolean enableBarcode;
    private boolean enableIDCard;
    private boolean rfUseSFI;           // 전자여권 Short File ID 적용.
    // private boolean authByPass;         // 전자여권 인증 bypass
    private boolean securityCheck;      // 홍콩 신분증 관련 특수 기능
    private boolean enableEnhanceIR;    // IR Image 밝기 보정
    private boolean enableEnhanceUV;
    private boolean enableEnhanceWH;
    private boolean antiGlare;          // anti-glare 기능 사용 여부
    private boolean antiGlareIR;        // IR 스캔에 anti-glare 기능 사용 여부
    private boolean antiGlareIRHalf;    // IR MRZ 스캔에 anti-glare 기능 사용 여부
    private int strengthWH;             // WH image 보정 강도 (-127 ~ 128 )
    private int strengthIR;             // IR image 보정 강도 (-127 ~ 128 )
    // private boolean enableUVSubtract;   // UV image 처리 보완
    // private boolean changeSC;           // 특수문자 일괄 치환 기능 사용 여부
    private boolean crop;
    
    // for image capture mode (IR, UV, WH)
    private boolean ir;
    private boolean uv;
    private boolean wh;
    
    // for E-PASSPORT
    private boolean dg1;
    private boolean dg2;
    private boolean dg3;
    private boolean dg4;
    private boolean dg5;
    private boolean dg6;
    private boolean dg7;
    private boolean dg8;
    private boolean dg9;
    private boolean dg10;
    private boolean dg11;
    private boolean dg12;
    private boolean dg13;
    private boolean dg14;
    private boolean dg15;
    private boolean dg16;

    private boolean pa;
    private boolean aa;
    private boolean ca;
    private boolean ta;
    private boolean sac;

    // for barcode configuration
    // private boolean enableCode11;
	// private boolean enableCode39;
	// private boolean enableCode93;
	// private boolean enableCode128;
	// private boolean enableCodabar;
	// private boolean enableInter2of5;
	// private boolean enablePatchCode;
	// private boolean enableEAN8;
	// private boolean enableUPCE;
	// private boolean enableEAN13;
	// private boolean enableUPCA;
	// private boolean enablePlus2;
	// private boolean enablePlus5;
	// private boolean enableCode39Extended;
	// private boolean enableUCC128;
	// private boolean enablePDF417;
	// private boolean enableDataMatrix;
	// private boolean enableQRCode;
	// private boolean enableMicroQRCode;
	// private boolean enableLeftToRight;
	// private boolean enableRightToLeft;
	// private boolean enableTopToBottom;
	// private boolean enableBottomToTop;
	// private int barcodesToRead;
	// private int threashold;
	// private int imageZoomRatio;

    public static SettingsForm from(FPHPSDeviceProperties deviceProperties) {
        return SettingsForm.builder()
            // .readTimeout(deviceProperties.getReadTimeout())
            // .detectDelayTime(deviceProperties.getDetectDelayTime())
            // .readMethod(deviceProperties.getReadMethod())
            // .userModeLed(deviceProperties.getUserModeLed())
            .checkRemove(deviceProperties.getCheckRemove() == 1 ? true : false)
            .enableRF(deviceProperties.getEnableRF() == 1 ? true : false)
            .enableBarcode(deviceProperties.getEnableBarcode() == 1 ? true : false)
            // .enableBuzzer(deviceProperties.getEnableBuzzer() == 1 ? true : false)
            .enableIDCard(deviceProperties.getEnableIDCard() == 1 ? true : false)
            .rfReadSize(deviceProperties.getRfReadSize())
            .rfUseSFI(deviceProperties.getRfUseSFI() == 1 ? true : false)
            // .authByPass(deviceProperties.getAuthByPass() == 1 ? true : false)
            .antiGlare(deviceProperties.getAntiGlare() == 1 ? true : false)
            .securityCheck(deviceProperties.getSecurityCheck() == 1 ? true : false)
            .enableEnhanceWH(deviceProperties.getEnableEnhanceWH() == 1 ? true : false)
            .enableEnhanceUV(deviceProperties.getEnableEnhanceUV() == 1 ? true : false)
            .antiGlareIR(deviceProperties.getAntiGlareIR() == 1 ? true : false)
            .antiGlareIRHalf(deviceProperties.getAntiGlareIRHalf() == 1 ? true : false)
            .enableEnhanceIR(deviceProperties.getEnableEnhanceIR() == 1 ? true : false)
            .strengthWH(deviceProperties.getStrengthWH())
            .strengthIR(deviceProperties.getStrengthIR())
            // .enableUVSubtract(deviceProperties.getEnableUVSubtract() == 1 ? true : false)
            // .changeSC(deviceProperties.getChangeSC() == 1 ? true : false)
            .crop(deviceProperties.getCrop() == 1 ? true : false)
            .ir(deviceProperties.getBatchModeProperties().getIr() == 1 ? true : false)
            .uv(deviceProperties.getBatchModeProperties().getUv() == 1 ? true : false)
            .wh(deviceProperties.getBatchModeProperties().getWh() == 1 ? true : false)
            .dg1(deviceProperties.getEPassportDGProperties().getDg1() == 1 ? true : false)
            .dg2(deviceProperties.getEPassportDGProperties().getDg2() == 1 ? true : false)
            .dg3(deviceProperties.getEPassportDGProperties().getDg3() == 1 ? true : false)
            .dg4(deviceProperties.getEPassportDGProperties().getDg4() == 1 ? true : false)
            .dg5(deviceProperties.getEPassportDGProperties().getDg5() == 1 ? true : false)
            .dg6(deviceProperties.getEPassportDGProperties().getDg6() == 1 ? true : false)
            .dg7(deviceProperties.getEPassportDGProperties().getDg7() == 1 ? true : false)
            .dg8(deviceProperties.getEPassportDGProperties().getDg8() == 1 ? true : false)
            .dg9(deviceProperties.getEPassportDGProperties().getDg9() == 1 ? true : false)
            .dg10(deviceProperties.getEPassportDGProperties().getDg10() == 1 ? true : false)
            .dg11(deviceProperties.getEPassportDGProperties().getDg11() == 1 ? true : false)
            .dg12(deviceProperties.getEPassportDGProperties().getDg12() == 1 ? true : false)
            .dg13(deviceProperties.getEPassportDGProperties().getDg13() == 1 ? true : false)
            .dg14(deviceProperties.getEPassportDGProperties().getDg14() == 1 ? true : false)
            .dg15(deviceProperties.getEPassportDGProperties().getDg15() == 1 ? true : false)
            .dg16(deviceProperties.getEPassportDGProperties().getDg16() == 1 ? true : false)
            .pa(deviceProperties.getEPassportAuthProperties().getPa() == 1 ? true : false)
            .aa(deviceProperties.getEPassportAuthProperties().getAa() == 1 ? true : false)
            .ca(deviceProperties.getEPassportAuthProperties().getCa() == 1 ? true : false)
            .ta(deviceProperties.getEPassportAuthProperties().getTa() == 1 ? true : false)
            .sac(deviceProperties.getEPassportAuthProperties().getSac() == 1 ? true : false)
            // .enableCode11(deviceProperties.getBarcodeConfigDTO().getEnableCode11() == 1 ? true : false)
            // .enableCode39(deviceProperties.getBarcodeConfigDTO().getEnableCode39() == 1 ? true : false)
            // .enableCode93(deviceProperties.getBarcodeConfigDTO().getEnableCode93() == 1 ? true : false)
            // .enableCode128(deviceProperties.getBarcodeConfigDTO().getEnableCode128() == 1 ? true : false)
            // .enableCodabar(deviceProperties.getBarcodeConfigDTO().getEnableCodabar() == 1 ? true : false)
            // .enableInter2of5(deviceProperties.getBarcodeConfigDTO().getEnableInter2of5() == 1 ? true : false)
            // .enablePatchCode(deviceProperties.getBarcodeConfigDTO().getEnablePatchCode() == 1 ? true : false)
            // .enableEAN8(deviceProperties.getBarcodeConfigDTO().getEnableEAN8() == 1 ? true : false)
            // .enableUPCE(deviceProperties.getBarcodeConfigDTO().getEnableUPCE() == 1 ? true : false)
            // .enableEAN13(deviceProperties.getBarcodeConfigDTO().getEnableEAN13() == 1 ? true : false)
            // .enableUPCA(deviceProperties.getBarcodeConfigDTO().getEnableUPCA() == 1 ? true : false)
            // .enablePlus2(deviceProperties.getBarcodeConfigDTO().getEnablePlus2() == 1 ? true : false)
            // .enablePlus5(deviceProperties.getBarcodeConfigDTO().getEnablePlus5() == 1 ? true : false)
            // .enableCode39Extended(deviceProperties.getBarcodeConfigDTO().getEnableCode39Extended() == 1 ? true : false)
            // .enableUCC128(deviceProperties.getBarcodeConfigDTO().getEnableUCC128() == 1 ? true : false)
            // .enablePDF417(deviceProperties.getBarcodeConfigDTO().getEnablePDF417() == 1 ? true : false)
            // .enableDataMatrix(deviceProperties.getBarcodeConfigDTO().getEnableDataMatrix() == 1 ? true : false)
            // .enableQRCode(deviceProperties.getBarcodeConfigDTO().getEnableQRCode() == 1 ? true : false)
            // .enableMicroQRCode(deviceProperties.getBarcodeConfigDTO().getEnableMicroQRCode() == 1 ? true : false)
            // .enableLeftToRight(deviceProperties.getBarcodeConfigDTO().getEnableLeftToRight() == 1 ? true : false)
            // .enableRightToLeft(deviceProperties.getBarcodeConfigDTO().getEnableRightToLeft() == 1 ? true : false)
            // .enableTopToBottom(deviceProperties.getBarcodeConfigDTO().getEnableTopToBottom() == 1 ? true : false)
            // .enableBottomToTop(deviceProperties.getBarcodeConfigDTO().getEnableBottomToTop() == 1 ? true : false)
            // .barcodesToRead(deviceProperties.getBarcodeConfigDTO().getBarcodesToRead())
            // .threashold(deviceProperties.getBarcodeConfigDTO().getThreashold())
            // .imageZoomRatio(deviceProperties.getBarcodeConfigDTO().getImageZoomRatio())
            .build();
    }

    public static FPHPSDeviceProperties to(SettingsForm settingsForm, FPHPSDeviceProperties deviceProperties) {
        // deviceProperties.setReadMethod(settingsForm.getReadMethod());
        deviceProperties.setCheckRemove(settingsForm.isCheckRemove() ? 1 : 0);
        deviceProperties.setEnableRF(settingsForm.isEnableRF() ? 1 : 0);
        // deviceProperties.setEnableBuzzer(settingsForm.isEnableBuzzer() ? 1 : 0);
        deviceProperties.setRfReadSize(settingsForm.getRfReadSize());
        // deviceProperties.setReadTimeout(settingsForm.getReadTimeout());
        // deviceProperties.setDetectDelayTime(settingsForm.getDetectDelayTime());
        deviceProperties.setEnableIDCard(settingsForm.isEnableIDCard() ? 1 : 0);
        deviceProperties.setEnableBarcode(settingsForm.isEnableBarcode() ? 1 : 0);
        // deviceProperties.setEnableUVSubtract(settingsForm.isEnableUVSubtract() ? 1 : 0);
        deviceProperties.setEnableEnhanceIR(settingsForm.isEnableEnhanceIR() ? 1 : 0);
        deviceProperties.setEnableEnhanceUV(settingsForm.isEnableEnhanceUV() ? 1 : 0);
        deviceProperties.setEnableEnhanceWH(settingsForm.isEnableEnhanceWH() ? 1 : 0);
        deviceProperties.setAntiGlare(settingsForm.isAntiGlare() ? 1 : 0);
        deviceProperties.setAntiGlareIR(settingsForm.isAntiGlareIR() ? 1 : 0);
        deviceProperties.setAntiGlareIRHalf(settingsForm.isAntiGlareIRHalf() ? 1 : 0);
        // deviceProperties.setAuthByPass(settingsForm.isAuthByPass() ? 1 : 0);
        deviceProperties.setSecurityCheck(settingsForm.isSecurityCheck() ? 1 : 0);
        // deviceProperties.setChangeSC(settingsForm.isChangeSC() ? 1 : 0);
        deviceProperties.setCrop(settingsForm.isCrop() ?  1 : 0);
        deviceProperties.setBatchModeProperties(
            BatchModeProperties.builder()
                .ir(settingsForm.isIr() ? 1 : 0)
                .uv(settingsForm.isUv() ? 1 : 0)
                .wh(settingsForm.isWh() ? 1 : 0)
                .build()
        );
        deviceProperties.setEPassportAuthProperties(
            EPassportAuthProperties.builder()
                .pa(settingsForm.isPa() ? 1 : 0)
                .aa(settingsForm.isAa() ? 1 : 0)
                .ca(settingsForm.isCa() ? 1 : 0)
                .ta(settingsForm.isTa() ? 1 : 0)
                .sac(settingsForm.isSac() ? 1 : 0)
                .build()
        );
        deviceProperties.setEPassportDGProperties(
            EPassportDGProperties.builder()
                .dg1(settingsForm.isDg1() ? 1 : 0)
                .dg2(settingsForm.isDg2() ? 1 : 0)
                .dg3(settingsForm.isDg3() ? 1 : 0)
                .dg4(settingsForm.isDg4() ? 1 : 0)
                .dg5(settingsForm.isDg5() ? 1 : 0)
                .dg6(settingsForm.isDg6() ? 1 : 0)
                .dg7(settingsForm.isDg7() ? 1 : 0)
                .dg8(settingsForm.isDg8() ? 1 : 0)
                .dg9(settingsForm.isDg9() ? 1 : 0)
                .dg10(settingsForm.isDg10() ? 1 : 0)
                .dg11(settingsForm.isDg11() ? 1 : 0)
                .dg12(settingsForm.isDg12() ? 1 : 0)
                .dg13(settingsForm.isDg13() ? 1 : 0)
                .dg14(settingsForm.isDg14() ? 1 : 0)
                .dg15(settingsForm.isDg15() ? 1 : 0)
                .dg16(settingsForm.isDg16() ? 1 : 0)
                .build()
        );
        return deviceProperties;
    }
}
