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
public class EPassportSettingForm implements Serializable {
    private boolean checkRemove;        // 이전 판독 여권 존재 검사
    private boolean enableRF;
    private int rfReadSize;             // 전자여권 읽기 크기 설정. 기본 231, 최대 4096
    private boolean rfUseSFI;           // 전자여권 Short File ID 적용.
    private boolean authByPass;         // 전자여권 인증 bypass
    private boolean enableEnhanceIR;    // IR Image 밝기 보정
    private boolean enableEnhanceUV;
    private boolean enableEnhanceWH;
    private boolean antiGlare;          // anti-glare 기능 사용 여부
    private boolean antiGlareIR;        // IR 스캔에 anti-glare 기능 사용 여부
    private boolean antiGlareIRHalf;    // IR MRZ 스캔에 anti-glare 기능 사용 여부
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

    public static EPassportSettingForm from(FPHPSDeviceProperties deviceProperties) {
        return EPassportSettingForm.builder()
            .checkRemove(deviceProperties.getCheckRemove() == 1 ? true : false)
            .enableRF(deviceProperties.getEnableRF() == 1 ? true : false)
            .rfReadSize(deviceProperties.getRfReadSize() <= 0 ? 231 : deviceProperties.getRfReadSize())
            .rfUseSFI(deviceProperties.getRfUseSFI() == 1 ? true : false)
            .authByPass(deviceProperties.getAuthByPass() == 1 ? true : false)
            .antiGlare(deviceProperties.getAntiGlare() == 1 ? true : false)
            .antiGlareIR(deviceProperties.getAntiGlareIR() == 1 ? true : false)
            .antiGlareIRHalf(deviceProperties.getAntiGlareIRHalf() == 1 ? true : false)
            .enableEnhanceWH(deviceProperties.getEnableEnhanceWH() == 1 ? true : false)
            .enableEnhanceUV(deviceProperties.getEnableEnhanceUV() == 1 ? true : false)
            .enableEnhanceIR(deviceProperties.getEnableEnhanceIR() == 1 ? true : false)
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
            .build();
    }

    public static FPHPSDeviceProperties to(EPassportSettingForm settingForm) {
        FPHPSDeviceProperties deviceProperties = new FPHPSDeviceProperties();
        deviceProperties.setCheckRemove(settingForm.isCheckRemove() ? 1 : 0);
        deviceProperties.setEnableRF(settingForm.isEnableRF() ? 1 : 0);
        deviceProperties.setRfReadSize(settingForm.getRfReadSize() <= 0 ? 231 : settingForm.getRfReadSize());
        deviceProperties.setEnableEnhanceIR(settingForm.isEnableEnhanceIR() ? 1 : 0);
        deviceProperties.setEnableEnhanceUV(settingForm.isEnableEnhanceUV() ? 1 : 0);
        deviceProperties.setEnableEnhanceWH(settingForm.isEnableEnhanceWH() ? 1 : 0);
        deviceProperties.setAntiGlare(settingForm.isAntiGlare() ? 1 : 0);
        deviceProperties.setAntiGlareIR(settingForm.isAntiGlareIR() ? 1 : 0);
        deviceProperties.setAntiGlareIRHalf(settingForm.isAntiGlareIRHalf() ? 1 : 0);
        deviceProperties.setAuthByPass(settingForm.isAuthByPass() ? 1 : 0);
        deviceProperties.setCrop(settingForm.isCrop() ?  1 : 0);
        deviceProperties.setBatchModeProperties(
            BatchModeProperties.builder()
                .ir(settingForm.isIr() ? 1 : 0)
                .uv(settingForm.isUv() ? 1 : 0)
                .wh(settingForm.isWh() ? 1 : 0)
                .build()
        );
        deviceProperties.setEPassportAuthProperties(
            EPassportAuthProperties.builder()
                .pa(settingForm.isPa() ? 1 : 0)
                .aa(settingForm.isAa() ? 1 : 0)
                .ca(settingForm.isCa() ? 1 : 0)
                .ta(settingForm.isTa() ? 1 : 0)
                .sac(settingForm.isSac() ? 1 : 0)
                .build()
        );
        deviceProperties.setEPassportDGProperties(
            EPassportDGProperties.builder()
                .dg1(settingForm.isDg1() ? 1 : 0)
                .dg2(settingForm.isDg2() ? 1 : 0)
                .dg3(settingForm.isDg3() ? 1 : 0)
                .dg4(settingForm.isDg4() ? 1 : 0)
                .dg5(settingForm.isDg5() ? 1 : 0)
                .dg6(settingForm.isDg6() ? 1 : 0)
                .dg7(settingForm.isDg7() ? 1 : 0)
                .dg8(settingForm.isDg8() ? 1 : 0)
                .dg9(settingForm.isDg9() ? 1 : 0)
                .dg10(settingForm.isDg10() ? 1 : 0)
                .dg11(settingForm.isDg11() ? 1 : 0)
                .dg12(settingForm.isDg12() ? 1 : 0)
                .dg13(settingForm.isDg13() ? 1 : 0)
                .dg14(settingForm.isDg14() ? 1 : 0)
                .dg15(settingForm.isDg15() ? 1 : 0)
                .dg16(settingForm.isDg16() ? 1 : 0)
                .build()
        );
        return deviceProperties;
    }
}
