package com.smartcoreinc.fphps.example.fphps_web_example.Services;

import com.smartcoreinc.fphps.dto.properties.BatchModeProperties;
import com.smartcoreinc.fphps.dto.properties.EPassportAuthProperties;
import com.smartcoreinc.fphps.dto.properties.EPassportDGProperties;
import com.smartcoreinc.fphps.dto.properties.FPHPSDeviceProperties;
import com.smartcoreinc.fphps.example.fphps_web_example.entity.DeviceSettings;
import com.smartcoreinc.fphps.example.fphps_web_example.repository.DeviceSettingsRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@Getter
public class DevicePropertiesService {

    private final DeviceSettingsRepository settingsRepository;
    private FPHPSDeviceProperties properties;

    public DevicePropertiesService(DeviceSettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
        // Initialize with default properties to avoid null pointers
        this.properties = new FPHPSDeviceProperties();
        log.info("DevicePropertiesService initialized with default properties.");
    }

    @PostConstruct
    public void init() {
        // 애플리케이션 시작 시 저장된 설정 로드
        loadSavedSettings();
    }

    /**
     * SQLite에서 저장된 설정을 로드
     */
    private void loadSavedSettings() {
        Optional<DeviceSettings> savedSettings = settingsRepository.findTopByOrderByIdDesc();
        if (savedSettings.isPresent()) {
            this.properties = toDeviceProperties(savedSettings.get());
            log.info("Loaded saved settings from SQLite database.");
        } else {
            log.info("No saved settings found. Using default properties.");
        }
    }

    public void setProperties(FPHPSDeviceProperties properties) {
        if (properties != null) {
            this.properties = properties;
            // SQLite에 설정 저장
            saveSettings(properties);
            log.info("Device properties have been set/updated and saved to database.");
        }
    }

    /**
     * 설정을 SQLite에 저장
     */
    private void saveSettings(FPHPSDeviceProperties properties) {
        try {
            DeviceSettings settings = toDeviceSettings(properties);

            // 기존 설정이 있으면 ID를 유지하여 업데이트
            Optional<DeviceSettings> existing = settingsRepository.findTopByOrderByIdDesc();
            if (existing.isPresent()) {
                settings.setId(existing.get().getId());
            }

            settingsRepository.save(settings);
            log.debug("Settings saved to SQLite database.");
        } catch (Exception e) {
            log.error("Failed to save settings to database: {}", e.getMessage(), e);
        }
    }

    /**
     * FPHPSDeviceProperties를 DeviceSettings 엔티티로 변환
     */
    private DeviceSettings toDeviceSettings(FPHPSDeviceProperties props) {
        return DeviceSettings.builder()
                .checkRemove(props.getCheckRemove() == 1)
                .enableRF(props.getEnableRF() == 1)
                .rfReadSize(props.getRfReadSize())
                .enableBarcode(props.getEnableBarcode() == 1)
                .enableIDCard(props.getEnableIDCard() == 1)
                .rfUseSFI(props.getRfUseSFI() == 1)
                .securityCheck(props.getSecurityCheck() == 1)
                .enableEnhanceIR(props.getEnableEnhanceIR() == 1)
                .enableEnhanceUV(props.getEnableEnhanceUV() == 1)
                .enableEnhanceWH(props.getEnableEnhanceWH() == 1)
                .antiGlare(props.getAntiGlare() == 1)
                .antiGlareIR(props.getAntiGlareIR() == 1)
                .antiGlareIRHalf(props.getAntiGlareIRHalf() == 1)
                .strengthWH(props.getStrengthWH())
                .strengthIR(props.getStrengthIR())
                .crop(props.getCrop() == 1)
                // Batch Mode
                .ir(props.getBatchModeProperties() != null && props.getBatchModeProperties().getIr() == 1)
                .uv(props.getBatchModeProperties() != null && props.getBatchModeProperties().getUv() == 1)
                .wh(props.getBatchModeProperties() != null && props.getBatchModeProperties().getWh() == 1)
                // E-Passport DG
                .dg1(props.getEPassportDGProperties() != null && props.getEPassportDGProperties().getDg1() == 1)
                .dg2(props.getEPassportDGProperties() != null && props.getEPassportDGProperties().getDg2() == 1)
                .dg3(props.getEPassportDGProperties() != null && props.getEPassportDGProperties().getDg3() == 1)
                .dg4(props.getEPassportDGProperties() != null && props.getEPassportDGProperties().getDg4() == 1)
                .dg5(props.getEPassportDGProperties() != null && props.getEPassportDGProperties().getDg5() == 1)
                .dg6(props.getEPassportDGProperties() != null && props.getEPassportDGProperties().getDg6() == 1)
                .dg7(props.getEPassportDGProperties() != null && props.getEPassportDGProperties().getDg7() == 1)
                .dg8(props.getEPassportDGProperties() != null && props.getEPassportDGProperties().getDg8() == 1)
                .dg9(props.getEPassportDGProperties() != null && props.getEPassportDGProperties().getDg9() == 1)
                .dg10(props.getEPassportDGProperties() != null && props.getEPassportDGProperties().getDg10() == 1)
                .dg11(props.getEPassportDGProperties() != null && props.getEPassportDGProperties().getDg11() == 1)
                .dg12(props.getEPassportDGProperties() != null && props.getEPassportDGProperties().getDg12() == 1)
                .dg13(props.getEPassportDGProperties() != null && props.getEPassportDGProperties().getDg13() == 1)
                .dg14(props.getEPassportDGProperties() != null && props.getEPassportDGProperties().getDg14() == 1)
                .dg15(props.getEPassportDGProperties() != null && props.getEPassportDGProperties().getDg15() == 1)
                .dg16(props.getEPassportDGProperties() != null && props.getEPassportDGProperties().getDg16() == 1)
                // E-Passport Auth
                .pa(props.getEPassportAuthProperties() != null && props.getEPassportAuthProperties().getPa() == 1)
                .aa(props.getEPassportAuthProperties() != null && props.getEPassportAuthProperties().getAa() == 1)
                .ca(props.getEPassportAuthProperties() != null && props.getEPassportAuthProperties().getCa() == 1)
                .ta(props.getEPassportAuthProperties() != null && props.getEPassportAuthProperties().getTa() == 1)
                .sac(props.getEPassportAuthProperties() != null && props.getEPassportAuthProperties().getSac() == 1)
                .build();
    }

    /**
     * DeviceSettings 엔티티를 FPHPSDeviceProperties로 변환
     */
    private FPHPSDeviceProperties toDeviceProperties(DeviceSettings settings) {
        FPHPSDeviceProperties props = new FPHPSDeviceProperties();

        props.setCheckRemove(settings.isCheckRemove() ? 1 : 0);
        props.setEnableRF(settings.isEnableRF() ? 1 : 0);
        props.setRfReadSize(settings.getRfReadSize());
        props.setEnableBarcode(settings.isEnableBarcode() ? 1 : 0);
        props.setEnableIDCard(settings.isEnableIDCard() ? 1 : 0);
        props.setRfUseSFI(settings.isRfUseSFI() ? 1 : 0);
        props.setSecurityCheck(settings.isSecurityCheck() ? 1 : 0);
        props.setEnableEnhanceIR(settings.isEnableEnhanceIR() ? 1 : 0);
        props.setEnableEnhanceUV(settings.isEnableEnhanceUV() ? 1 : 0);
        props.setEnableEnhanceWH(settings.isEnableEnhanceWH() ? 1 : 0);
        props.setAntiGlare(settings.isAntiGlare() ? 1 : 0);
        props.setAntiGlareIR(settings.isAntiGlareIR() ? 1 : 0);
        props.setAntiGlareIRHalf(settings.isAntiGlareIRHalf() ? 1 : 0);
        props.setStrengthWH(settings.getStrengthWH());
        props.setStrengthIR(settings.getStrengthIR());
        props.setCrop(settings.isCrop() ? 1 : 0);

        // Batch Mode Properties
        props.setBatchModeProperties(BatchModeProperties.builder()
                .ir(settings.isIr() ? 1 : 0)
                .uv(settings.isUv() ? 1 : 0)
                .wh(settings.isWh() ? 1 : 0)
                .build());

        // E-Passport DG Properties
        props.setEPassportDGProperties(EPassportDGProperties.builder()
                .dg1(settings.isDg1() ? 1 : 0)
                .dg2(settings.isDg2() ? 1 : 0)
                .dg3(settings.isDg3() ? 1 : 0)
                .dg4(settings.isDg4() ? 1 : 0)
                .dg5(settings.isDg5() ? 1 : 0)
                .dg6(settings.isDg6() ? 1 : 0)
                .dg7(settings.isDg7() ? 1 : 0)
                .dg8(settings.isDg8() ? 1 : 0)
                .dg9(settings.isDg9() ? 1 : 0)
                .dg10(settings.isDg10() ? 1 : 0)
                .dg11(settings.isDg11() ? 1 : 0)
                .dg12(settings.isDg12() ? 1 : 0)
                .dg13(settings.isDg13() ? 1 : 0)
                .dg14(settings.isDg14() ? 1 : 0)
                .dg15(settings.isDg15() ? 1 : 0)
                .dg16(settings.isDg16() ? 1 : 0)
                .build());

        // E-Passport Auth Properties
        props.setEPassportAuthProperties(EPassportAuthProperties.builder()
                .pa(settings.isPa() ? 1 : 0)
                .aa(settings.isAa() ? 1 : 0)
                .ca(settings.isCa() ? 1 : 0)
                .ta(settings.isTa() ? 1 : 0)
                .sac(settings.isSac() ? 1 : 0)
                .build());

        return props;
    }
}
