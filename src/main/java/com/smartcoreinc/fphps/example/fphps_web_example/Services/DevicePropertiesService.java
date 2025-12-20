package com.smartcoreinc.fphps.example.fphps_web_example.Services;

import com.smartcoreinc.fphps.dto.properties.FPHPSDeviceProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Getter
public class DevicePropertiesService {

    private FPHPSDeviceProperties properties;

    public DevicePropertiesService() {
        // Initialize with default properties to avoid null pointers
        this.properties = new FPHPSDeviceProperties();
        log.info("DevicePropertiesService initialized with default properties.");
    }

    public void setProperties(FPHPSDeviceProperties properties) {
        if (properties != null) {
            this.properties = properties;
            log.info("Device properties have been set/updated.");
        }
    }
}
