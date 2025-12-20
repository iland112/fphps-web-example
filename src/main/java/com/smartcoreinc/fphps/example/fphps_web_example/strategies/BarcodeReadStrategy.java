package com.smartcoreinc.fphps.example.fphps_web_example.strategies;

import com.smartcoreinc.fphps.dto.DocumentReadResponse;
import com.smartcoreinc.fphps.dto.properties.FPHPSDeviceProperties;
import com.smartcoreinc.fphps.example.fphps_web_example.Services.DevicePropertiesService;
import com.smartcoreinc.fphps.example.fphps_web_example.config.handler.FastPassWebSocketHandler;
import com.smartcoreinc.fphps.infrastructure.FPHPSLibrary.FPHPS_READ_TYPES;
import com.smartcoreinc.fphps.manager.FPHPSDevice;
import com.smartcoreinc.fphps.readers.BarcodeReader;
import org.springframework.stereotype.Component;

@Component
public class BarcodeReadStrategy implements DocumentReadStrategy {

    private final DevicePropertiesService devicePropertiesService;

    public BarcodeReadStrategy(DevicePropertiesService devicePropertiesService) {
        this.devicePropertiesService = devicePropertiesService;
    }

    @Override
    public boolean supports(String docType) {
        return "BARCODE".equalsIgnoreCase(docType);
    }

    @Override
    public DocumentReadResponse read(FPHPSDevice device, FastPassWebSocketHandler fastPassWebSocketHandler, boolean isAuto) {
        FPHPSDeviceProperties properties = devicePropertiesService.getProperties();

        // Ensure properties are set for Barcode reading
        properties.setEnableBarcode(1);
        properties.setEnableRF(0); // Explicitly disable RF
        properties.setEnableIDCard(0); // Explicitly disable ID Card reading

        device.getDeviceSetting().setDeviceProperties(properties);

        BarcodeReader reader = new BarcodeReader(device, fastPassWebSocketHandler);
        return reader.read(FPHPS_READ_TYPES.FPHPS_RT_BARCODE, isAuto);
    }
}