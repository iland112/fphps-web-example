package com.smartcoreinc.fphps.example.fphps_web_example.strategies;

import com.smartcoreinc.fphps.dto.DocumentReadResponse;
import com.smartcoreinc.fphps.dto.properties.BatchModeProperties;
import com.smartcoreinc.fphps.dto.properties.FPHPSDeviceProperties;
import com.smartcoreinc.fphps.example.fphps_web_example.config.handler.FastPassWebSocketHandler;
import com.smartcoreinc.fphps.infrastructure.FPHPSLibrary.FPHPS_READ_TYPES;
import com.smartcoreinc.fphps.manager.FPHPSDevice;
import com.smartcoreinc.fphps.readers.BarcodeReader;
import org.springframework.stereotype.Component;

@Component
public class BarcodeReadStrategy implements DocumentReadStrategy {

    @Override
    public boolean supports(String docType) {
        return "BARCODE".equalsIgnoreCase(docType);
    }

    @Override
    public DocumentReadResponse read(FPHPSDevice device, FastPassWebSocketHandler fastPassWebSocketHandler, boolean isAuto) {
        FPHPSDeviceProperties deviceProperties = device.getDeviceProperties();
        deviceProperties.setCrop(1);
        deviceProperties.setCheckRemove(1);

        deviceProperties.setEnableBarcode(1);
        deviceProperties.setBatchModeProperties(
            BatchModeProperties.builder()
                .ir(0)
                .uv(0)
                .wh(1)
                .build()  
        );
        device.getDeviceSetting().setDeviceProperties(deviceProperties);

        BarcodeReader reader = new BarcodeReader(device, fastPassWebSocketHandler);
        return reader.read(FPHPS_READ_TYPES.FPHPS_RT_BARCODE, isAuto);
    }
}
