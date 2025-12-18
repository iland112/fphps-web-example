package com.smartcoreinc.fphps.example.fphps_web_example.Services;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.smartcoreinc.fphps.dto.DeviceInfo;
import com.smartcoreinc.fphps.dto.DocumentReadResponse;
import com.smartcoreinc.fphps.dto.FPHPSImage;
import com.smartcoreinc.fphps.dto.properties.BatchModeProperties;
import com.smartcoreinc.fphps.dto.properties.FPHPSDeviceProperties;
import com.smartcoreinc.fphps.example.fphps_web_example.config.handler.FastPassWebSocketHandler;
import com.smartcoreinc.fphps.manager.FPHPSDevice;
import com.smartcoreinc.fphps.manager.FPHPSDeviceManager;
import com.smartcoreinc.fphps.readers.PageScanner;
import com.smartcoreinc.fphps.example.fphps_web_example.strategies.DocumentReadStrategy;
import com.smartcoreinc.fphps.example.fphps_web_example.exceptions.DeviceOperationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public final class FPHPSService {

    private final FPHPSDeviceManager deviceManager;
    private final FastPassWebSocketHandler fastPassWebSocketHandler;
    private final List<DocumentReadStrategy> strategies;
    
    private FPHPSDevice device;

    public FPHPSService(FastPassWebSocketHandler fastPassWebSocketHandler, List<DocumentReadStrategy> strategies) {
        this.fastPassWebSocketHandler = fastPassWebSocketHandler;
        this.strategies = strategies;
        this.deviceManager = FPHPSDeviceManager.getInstance();
        try {
            initDevices();
        } catch (com.smartcoreinc.fphps.exception.FPHPSException e) {
            log.error("Failed to initialize FPHPS devices: {}", e.getMessage(), e);
            throw new DeviceOperationException("Failed to initialize FPHPS devices: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("An unexpected error during device initialization: {}", e.getMessage(), e);
            throw new DeviceOperationException("An unexpected error occurred during device initialization: " + e.getMessage(), e);
        }
    }

    private void initDevices() {
        this.deviceManager.enumerateDevices();
        this.device = this.deviceManager.getDevice();
    }

    public DeviceInfo getDeviceInfo() {
        return device.getDeviceInfo();
    }

    private <R> R executeWithDevice(Function<FPHPSDevice, R> action) {
        try {
            device.openDevice();
            try {
                return action.apply(device);
            } catch (com.smartcoreinc.fphps.exception.FPHPSException e) {
                log.error("FPHPS device operation failed: {}", e.getMessage(), e);
                throw new DeviceOperationException("FPHPS device error: " + e.getMessage(), e);
            } catch (Exception e) {
                log.error("An unexpected error during device operation: {}", e.getMessage(), e);
                throw new DeviceOperationException("An unexpected error occurred during device operation: " + e.getMessage(), e);
            }
        } finally {
            // 성공, 실패 여부와 관계없이 항상 장치를 닫습니다.
            if (device.isDeviceOpened()) {
                device.closeDevice();
            }
        }
    }

    // 반환 값이 없는 동작을 위한 메소드도 추가할 수 있습니다.
    private void executeWithDevice(Consumer<FPHPSDevice> action) {
        try {
            device.openDevice();
            try {
                action.accept(device);
            } catch (com.smartcoreinc.fphps.exception.FPHPSException e) {
                log.error("FPHPS device operation failed: {}", e.getMessage(), e);
                throw new DeviceOperationException("FPHPS device error: " + e.getMessage(), e);
            } catch (Exception e) {
                log.error("An unexpected error during device operation: {}", e.getMessage(), e);
                throw new DeviceOperationException("An unexpected error occurred during device operation: " + e.getMessage(), e);
            }
        } finally {
            if (device.isDeviceOpened()) {
                device.closeDevice();
            }
        }
    }

    public FPHPSDeviceProperties getCurrentDeviceProperties() {
        return executeWithDevice(openedDevice -> {
            return openedDevice.getDeviceProperties();
        });
    }

    public void setDeviceProperties(FPHPSDeviceProperties deviceProperties) {
        this.device.getDeviceSetting().setDeviceProperties(deviceProperties);
    }

    public DocumentReadResponse read(String docType, boolean isAuto) {
        return executeWithDevice(openedDevice -> {
            for (DocumentReadStrategy strategy : strategies) {
                if (strategy.supports(docType)) {
                    return strategy.read(openedDevice, fastPassWebSocketHandler, isAuto);
                }
            }
            log.warn("No strategy found for document type: {}", docType);
            return null; // Or throw an exception
        });
    }

    public void closeDevice() {
        device.closeDevice();
    }

    public FPHPSImage scanPage(int lightType) {
        return executeWithDevice(openedDevice -> {
            FPHPSDeviceProperties deviceProperties = openedDevice.getDeviceProperties();
            deviceProperties.setCrop(1);
            deviceProperties.setCheckRemove(1);
            deviceProperties.setBatchModeProperties(
                    BatchModeProperties.builder()
                            .ir(1)
                            .uv(1)
                            .wh(1)
                            .build());
            openedDevice.getDeviceSetting().setDeviceProperties(deviceProperties);

            PageScanner scanner = new PageScanner(openedDevice);
            return scanner.scan(lightType);
        });
    }

}
