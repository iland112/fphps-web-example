package com.smartcoreinc.fphps.example.fphps_web_example.Services;

import org.springframework.stereotype.Service;

import com.smartcoreinc.fphps.dto.DeviceInfo;
import com.smartcoreinc.fphps.dto.DocumentReadResponse;
import com.smartcoreinc.fphps.dto.FPHPSImage;
import com.smartcoreinc.fphps.dto.properties.BatchModeProperties;
import com.smartcoreinc.fphps.dto.properties.EPassportAuthProperties;
import com.smartcoreinc.fphps.dto.properties.EPassportDGProperties;
import com.smartcoreinc.fphps.dto.properties.FPHPSDeviceProperties;
import com.smartcoreinc.fphps.example.fphps_web_example.config.handler.FastPassWebSocketHandler;
import com.smartcoreinc.fphps.infrastructure.FPHPSLibrary.FPHPS_READ_TYPES;
import com.smartcoreinc.fphps.manager.FPHPSDevice;
import com.smartcoreinc.fphps.manager.FPHPSDeviceManager;
import com.smartcoreinc.fphps.readers.AbstractReader;
import com.smartcoreinc.fphps.readers.BarcodeReader;
import com.smartcoreinc.fphps.readers.EPassportReader;
import com.smartcoreinc.fphps.readers.IDCardReader;
import com.smartcoreinc.fphps.readers.PageScanner;
import com.smartcoreinc.fphps.readers.PassportReader;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public final class FPHPSService {

    private final FPHPSDeviceManager deviceManager;
    private final FastPassWebSocketHandler fastPassWebSocketHandler;
    
    private FPHPSDevice device;

    public FPHPSService(FastPassWebSocketHandler fastPassWebSocketHandler) {
        this.fastPassWebSocketHandler = fastPassWebSocketHandler;
        this.deviceManager = FPHPSDeviceManager.getInstance();
    }

    public void initDevices() {
        this.deviceManager.enumerateDevices();
        this.device = this.deviceManager.getDevice();
    }

    public DeviceInfo getDeviceInfo() {
        return device.getDeviceInfo();
    }

    public FPHPSDeviceProperties getCurrentDeviceProperties() {
        if (!this.device.isDeviceOpened()) {
            this.device.openDevice();
        }
        FPHPSDeviceProperties deviceProperties = this.device.getDeviceProperties();
        this.device.closeDevice();
        return deviceProperties;
    }

    public void setDeviceProperties(FPHPSDeviceProperties deviceProperties) {
        this.device.getDeviceSetting().setDeviceProperties(deviceProperties);
    }

    public DocumentReadResponse read(String docType, boolean isAuto) {
        DocumentReadResponse response = null;
        try {
            if (device.isDeviceOpened()) {
                device.openDevice();
            }
            int readType = 1;   // Default Passport

            FPHPSDeviceProperties deviceProperties = this.device.getDeviceProperties();
            deviceProperties.setCrop(1);
            deviceProperties.setCheckRemove(1);

            AbstractReader reader;
            
            switch (docType) {
                case "PASSPORT":
                    readType = FPHPS_READ_TYPES.FPHPS_RT_PASSPORT;
                    reader = new EPassportReader(device, fastPassWebSocketHandler);
                    deviceProperties.setEnableRF(1);
                    deviceProperties.setBatchModeProperties(
                        BatchModeProperties.builder()
                            .ir(1)
                            .uv(1)
                            .wh(1)
                            .build()  
                    );
                    deviceProperties.setEPassportAuthProperties(
                        EPassportAuthProperties.builder()
                            .pa(1)
                            .aa(1)
                            .ca(0)
                            .ta(0)
                            .sac(0)
                            .build()    
                    );
                    deviceProperties.setEPassportDGProperties(
                        EPassportDGProperties.builder()
                            .dg1(1)
                            .dg2(1)
                            .dg3(1)
                            .dg4(1)
                            .dg5(1)
                            .dg6(1)
                            .dg7(1)
                            .dg8(1)
                            .dg9(1)
                            .dg10(1)
                            .dg11(1)
                            .dg12(1)
                            .dg13(1)
                            .dg14(1)
                            .dg15(1)
                            .dg16(1)
                            .build()
                    );
                    break;
                case "IDCARD":
                    readType = FPHPS_READ_TYPES.FPHPS_RT_PASSPORT;
                    reader = new IDCardReader(device, fastPassWebSocketHandler);
                    deviceProperties.setEnableIDCard(1);
                    deviceProperties.setEnableRF(1);
                    deviceProperties.setBatchModeProperties(
                        BatchModeProperties.builder()
                            .ir(1)
                            .uv(1)
                            .wh(1)
                            .build()  
                    );
                    deviceProperties.setEPassportAuthProperties(
                        EPassportAuthProperties.builder()
                            .pa(1)
                            .aa(1)
                            .ca(0)
                            .ta(0)
                            .sac(0)
                            .build()    
                    );
                    deviceProperties.setEPassportDGProperties(
                        EPassportDGProperties.builder()
                            .dg1(1)
                            .dg2(1)
                            .dg3(1)
                            .dg4(1)
                            .dg5(1)
                            .dg6(1)
                            .dg7(1)
                            .dg8(1)
                            .dg9(1)
                            .dg10(1)
                            .dg11(1)
                            .dg12(1)
                            .dg13(1)
                            .dg14(1)
                            .dg15(1)
                            .dg16(1)
                            .build()
                    );
                    break;
                case "BARCODE":
                    readType = FPHPS_READ_TYPES.FPHPS_RT_BARCODE;
                    reader = new BarcodeReader(device, fastPassWebSocketHandler); 
                    deviceProperties.setEnableBarcode(1);
                    deviceProperties.setBatchModeProperties(
                        BatchModeProperties.builder()
                            .ir(0)
                            .uv(0)
                            .wh(1)
                            .build()  
                    );
                    break;
                default:
                    reader = new PassportReader(device, fastPassWebSocketHandler);
                    break;
            }
            device.getDeviceSetting().setDeviceProperties(deviceProperties);

            response = reader.read(readType, isAuto);
            return response;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return response;
    }

    public void closeDevice() {
        device.closeDevice();
    }

    public FPHPSImage scanPage(int lightType) {
        if (device.isDeviceOpened()) {
            device.openDevice();
        }
        FPHPSDeviceProperties deviceProperties = device.getDeviceProperties();
        deviceProperties.setCrop(1);
        deviceProperties.setCheckRemove(1);
        deviceProperties.setBatchModeProperties(
            BatchModeProperties.builder()
            .ir(1)
            .uv(1)
            .wh(1)
            .build()  
        );
        device.getDeviceSetting().setDeviceProperties(deviceProperties);

        PageScanner scanner = new PageScanner(device);
        // FPHPSImage image = scanner.scan(lightType, FPHPS_IMAGE_FORMAT.FPHPS_IF_JPG, 1, false);
        FPHPSImage image = scanner.scan(lightType);
        device.closeDevice();
        return image;
    }

}
