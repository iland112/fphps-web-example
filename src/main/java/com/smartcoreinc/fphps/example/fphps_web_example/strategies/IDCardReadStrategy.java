package com.smartcoreinc.fphps.example.fphps_web_example.strategies;

import com.smartcoreinc.fphps.dto.DocumentReadResponse;
import com.smartcoreinc.fphps.dto.properties.FPHPSDeviceProperties;
import com.smartcoreinc.fphps.example.fphps_web_example.Services.DevicePropertiesService;
import com.smartcoreinc.fphps.example.fphps_web_example.config.handler.FastPassWebSocketHandler;
import com.smartcoreinc.fphps.infrastructure.FPHPSLibrary.FPHPS_READ_TYPES;
import com.smartcoreinc.fphps.manager.FPHPSDevice;
import com.smartcoreinc.fphps.readers.IDCardReader;
import org.springframework.stereotype.Component;

@Component
public class IDCardReadStrategy implements DocumentReadStrategy {

    private final DevicePropertiesService devicePropertiesService;

    public IDCardReadStrategy(DevicePropertiesService devicePropertiesService) {
        this.devicePropertiesService = devicePropertiesService;
    }

    @Override
    public boolean supports(String docType) {
        return "IDCARD".equalsIgnoreCase(docType);
    }

    @Override
    public DocumentReadResponse read(FPHPSDevice device, FastPassWebSocketHandler fastPassWebSocketHandler, boolean isAuto) {
        FPHPSDeviceProperties properties = devicePropertiesService.getProperties();

        // Ensure properties are set for ID Card reading
        properties.setEnableIDCard(1);
        properties.setEnableRF(1); // Keep RF enabled for e-ID cards
        properties.setEnableBarcode(0); // Explicitly disable Barcode reading

        device.getDeviceSetting().setDeviceProperties(properties);
        
        IDCardReader reader = new IDCardReader(device, fastPassWebSocketHandler);
        // NOTE: Original code used FPHPS_READ_TYPES.FPHPS_RT_PASSPORT.
        // Assuming this is still the intended value if no specific FPHPS_RT_IDCARD exists.
        return reader.read(FPHPS_READ_TYPES.FPHPS_RT_PASSPORT, isAuto);
    }
}