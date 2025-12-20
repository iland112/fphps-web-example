package com.smartcoreinc.fphps.example.fphps_web_example.strategies;

import com.smartcoreinc.fphps.dto.DocumentReadResponse;
import com.smartcoreinc.fphps.dto.properties.FPHPSDeviceProperties;
import com.smartcoreinc.fphps.example.fphps_web_example.Services.DevicePropertiesService;
import com.smartcoreinc.fphps.example.fphps_web_example.config.handler.FastPassWebSocketHandler;
import com.smartcoreinc.fphps.infrastructure.FPHPSLibrary.FPHPS_READ_TYPES;
import com.smartcoreinc.fphps.manager.FPHPSDevice;
import com.smartcoreinc.fphps.readers.EPassportReader;
import org.springframework.stereotype.Component;

@Component
public class PassportReadStrategy implements DocumentReadStrategy {

    private final DevicePropertiesService devicePropertiesService;

    public PassportReadStrategy(DevicePropertiesService devicePropertiesService) {
        this.devicePropertiesService = devicePropertiesService;
    }

    @Override
    public boolean supports(String docType) {
        return "PASSPORT".equalsIgnoreCase(docType);
    }

    @Override
    public DocumentReadResponse read(FPHPSDevice device, FastPassWebSocketHandler fastPassWebSocketHandler, boolean isAuto) {
        FPHPSDeviceProperties properties = devicePropertiesService.getProperties();
        
        // Ensure properties are set for Passport reading
        properties.setEnableRF(1);
        properties.setEnableIDCard(0); // Explicitly disable ID Card reading
        properties.setEnableBarcode(0); // Explicitly disable Barcode reading

        device.getDeviceSetting().setDeviceProperties(properties);

        EPassportReader reader = new EPassportReader(device, fastPassWebSocketHandler);
        return reader.read(FPHPS_READ_TYPES.FPHPS_RT_PASSPORT, isAuto);
    }
}