package com.smartcoreinc.fphps.example.fphps_web_example.Services;

import org.springframework.stereotype.Service;

import com.smartcoreinc.fphps.dto.DeviceInfo;
import com.smartcoreinc.fphps.dto.DocumentReadResponse;
import com.smartcoreinc.fphps.dto.FPHPSImage;
import com.smartcoreinc.fphps.dto.properties.BatchModeProperties;
import com.smartcoreinc.fphps.dto.properties.DeviceProperties;
import com.smartcoreinc.fphps.dto.properties.EPassportAuthProperties;
import com.smartcoreinc.fphps.dto.properties.EPassportDGProperties;
import com.smartcoreinc.fphps.example.fphps_web_example.config.handler.FastPassWebSocketHandler;
import com.smartcoreinc.fphps.infrastructure.FPHPSLibrary.FPHPS_READ_TYPES;
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

    private final FastPassWebSocketHandler fastPassWebSocketHandler;
    
    private AbstractReader reader;
    private DeviceProperties deviceProperties;

    public FPHPSService(FastPassWebSocketHandler fastPassWebSocketHandler) {
        this.fastPassWebSocketHandler = fastPassWebSocketHandler;
    }

    public DeviceInfo getDeviceInfo() {
        this.reader = new PassportReader(fastPassWebSocketHandler);
        DeviceInfo deviceInfo = this.reader.getDeviceInfo();
        return deviceInfo;
    }

    public void connectToDevice() {
        if (this.reader == null) {
            this.reader = new PassportReader(fastPassWebSocketHandler);
        }
        this.reader.openDevice();
    }

    public DeviceProperties getCurrentDeviceProperties() {
        if (this.reader == null) {
            this.reader = new PassportReader(fastPassWebSocketHandler);
            this.reader.openDevice();
        } else {
            if (!this.reader.isDeviceOpened()) {
                this.reader.openDevice();
            }
        }
        DeviceProperties deviceProperties = this.reader.getCurrentDeviceProperties();
        this.deviceProperties = deviceProperties;
        return this.deviceProperties;
    }

    public void setDeviceProperties(DeviceProperties deviceProperties) {
        this.reader.setDeviceProperties(deviceProperties);
    }

    public DocumentReadResponse read(String docType, boolean isAuto) {
        
        int readType = 1;   // Default Passport

        if (this.deviceProperties == null) {
            this.deviceProperties = new DeviceProperties();
        }
        this.deviceProperties.setCrop(1);
        this.deviceProperties.setCheckRemove(1);
        
        switch (docType) {
            case "PASSPORT":
                readType = FPHPS_READ_TYPES.FPHPS_RT_PASSPORT;
                reader = new EPassportReader(fastPassWebSocketHandler);
                this.deviceProperties.setEnableRF(1);
                this.deviceProperties.setBatchModeProperties(
                    BatchModeProperties.builder()
                        .ir(1)
                        .uv(1)
                        .wh(1)
                        .build()  
                );
                this.deviceProperties.setEPassportAuthProperties(
                    EPassportAuthProperties.builder()
                        .pa(1)
                        .aa(1)
                        .ca(0)
                        .ta(0)
                        .sac(0)
                        .build()    
                );
                this.deviceProperties.setEPassportDGProperties(
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
                reader = new IDCardReader(fastPassWebSocketHandler);
                this.deviceProperties.setEnableIDCard(1);
                this.deviceProperties.setEnableRF(1);
                this.deviceProperties.setBatchModeProperties(
                    BatchModeProperties.builder()
                        .ir(1)
                        .uv(1)
                        .wh(1)
                        .build()  
                );
                this.deviceProperties.setEPassportAuthProperties(
                    EPassportAuthProperties.builder()
                        .pa(1)
                        .aa(1)
                        .ca(0)
                        .ta(0)
                        .sac(0)
                        .build()    
                );
                this.deviceProperties.setEPassportDGProperties(
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
                reader = new BarcodeReader(fastPassWebSocketHandler); 
                this.deviceProperties.setEnableBarcode(1);
                this.deviceProperties.setBatchModeProperties(
                    BatchModeProperties.builder()
                        .ir(0)
                        .uv(0)
                        .wh(1)
                        .build()  
                );
                break;
            default:
                reader = new PassportReader(fastPassWebSocketHandler);
                break;
        }
        DocumentReadResponse response = this.reader.read(this.deviceProperties, readType, isAuto);
        return response;
    }

    public void closeDevice() {
        this.reader.closeDevice();
    }

    public FPHPSImage scanPage(int lightType) {
        PageScanner scanner = new PageScanner();
        if (this.deviceProperties == null) {
            this.deviceProperties = new DeviceProperties();
        }
        this.deviceProperties.setCrop(1);
        this.deviceProperties.setCheckRemove(1);
        this.deviceProperties.setBatchModeProperties(
            BatchModeProperties.builder()
                .ir(1)
                .uv(1)
                .wh(1)
                .build()  
        );
        // FPHPSImage image = scanner.scan(lightType, FPHPS_IMAGE_FORMAT.FPHPS_IF_JPG, 1, false);
        FPHPSImage image = scanner.scan(deviceProperties, lightType);
        return image;
    }

}
