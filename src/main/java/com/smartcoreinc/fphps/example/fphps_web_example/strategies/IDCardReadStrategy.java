package com.smartcoreinc.fphps.example.fphps_web_example.strategies;

import com.smartcoreinc.fphps.dto.DocumentReadResponse;
import com.smartcoreinc.fphps.dto.properties.BatchModeProperties;
import com.smartcoreinc.fphps.dto.properties.EPassportAuthProperties;
import com.smartcoreinc.fphps.dto.properties.EPassportDGProperties;
import com.smartcoreinc.fphps.dto.properties.FPHPSDeviceProperties;
import com.smartcoreinc.fphps.example.fphps_web_example.config.handler.FastPassWebSocketHandler;
import com.smartcoreinc.fphps.infrastructure.FPHPSLibrary.FPHPS_READ_TYPES;
import com.smartcoreinc.fphps.manager.FPHPSDevice;
import com.smartcoreinc.fphps.readers.IDCardReader;
import org.springframework.stereotype.Component;

@Component
public class IDCardReadStrategy implements DocumentReadStrategy {

    @Override
    public boolean supports(String docType) {
        return "IDCARD".equalsIgnoreCase(docType);
    }

    @Override
    public DocumentReadResponse read(FPHPSDevice device, FastPassWebSocketHandler fastPassWebSocketHandler, boolean isAuto) {
        FPHPSDeviceProperties deviceProperties = device.getDeviceProperties();
        deviceProperties.setCrop(1);
        deviceProperties.setCheckRemove(1);

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
        device.getDeviceSetting().setDeviceProperties(deviceProperties);

        IDCardReader reader = new IDCardReader(device, fastPassWebSocketHandler);
        // NOTE: Original code used FPHPS_READ_TYPES.FPHPS_RT_PASSPORT.
        // Assuming this is still the intended value if no specific FPHPS_RT_IDCARD exists.
        return reader.read(FPHPS_READ_TYPES.FPHPS_RT_PASSPORT, isAuto);
    }
}
