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

/**
 * Service class for interacting with the FPHPS (FastPass Hybrid Passport System) device
 * through its JNA-wrapped native library. This service manages device initialization,
 * property settings, document reading operations, and error handling for native calls.
 */
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
        // Obtain an instance of the FPHPSDeviceManager, which is the entry point to the JNA-wrapped native library.
        this.deviceManager = FPHPSDeviceManager.getInstance();
        try {
            // Initialize devices by enumerating available native devices.
            initDevices();
        } catch (com.smartcoreinc.fphps.exception.FPHPSException e) {
            log.error("Failed to initialize FPHPS devices: {}", e.getMessage(), e);
            throw new DeviceOperationException("Failed to initialize FPHPS devices: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("An unexpected error during device initialization: {}", e.getMessage(), e);
            throw new DeviceOperationException("An unexpected error occurred during device initialization: " + e.getMessage(), e);
        }
    }

    /**
     * Enumerates available FPHPS devices and retrieves the primary device.
     * This method directly interacts with the JNA-wrapped native library.
     */
    private void initDevices() {
        this.deviceManager.enumerateDevices();
        this.device = this.deviceManager.getDevice();
    }

    /**
     * Retrieves the device information from the FPHPS device.
     * This method interacts with the JNA-wrapped native library through `FPHPSDevice.getDeviceInfo()`.
     * @return DeviceInfo object containing current device details.
     */
    public DeviceInfo getDeviceInfo() {
        return device.getDeviceInfo();
    }

    /**
     * Executes an action with the FPHPS device, handling opening and closing of the device.
     * This ensures that the native device is properly managed during JNA calls.
     *
     * @param action The function to execute with the opened FPHPSDevice.
     * @param <R> The return type of the action.
     * @return The result of the action.
     * @throws DeviceOperationException if a native device operation fails.
     */
    private <R> R executeWithDevice(Function<FPHPSDevice, R> action) {
        try {
            // Open the native device before performing any operation.
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
            // Always close the native device, regardless of success or failure.
            if (device.isDeviceOpened()) {
                device.closeDevice();
            }
        }
    }

    /**
     * Executes a void action with the FPHPS device, handling opening and closing of the device.
     * This ensures that the native device is properly managed during JNA calls.
     *
     * @param action The consumer to execute with the opened FPHPSDevice.
     * @throws DeviceOperationException if a native device operation fails.
     */
    private void executeWithDevice(Consumer<FPHPSDevice> action) {
        try {
            // Open the native device before performing any operation.
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
            // Always close the native device, regardless of success or failure.
            if (device.isDeviceOpened()) {
                device.closeDevice();
            }
        }
    }

    /**
     * Retrieves the current device properties from the FPHPS device.
     * This method interacts with the JNA-wrapped native library to get device settings.
     * @return FPHPSDeviceProperties object.
     */
    public FPHPSDeviceProperties getCurrentDeviceProperties() {
        return executeWithDevice(openedDevice -> {
            return openedDevice.getDeviceProperties();
        });
    }

    /**
     * Sets the device properties for the FPHPS device.
     * This method sends the new properties to the JNA-wrapped native library.
     * @param deviceProperties The FPHPSDeviceProperties to set.
     */
    public void setDeviceProperties(FPHPSDeviceProperties deviceProperties) {
        // Direct interaction with the JNA-wrapped device setting.
        this.device.getDeviceSetting().setDeviceProperties(deviceProperties);
    }

    /**
     * Initiates a document read operation on the FPHPS device.
     * This method orchestrates calls to the JNA-wrapped native library via a DocumentReadStrategy.
     * @param docType The type of document to read (e.g., "PASSPORT", "IDCARD", "BARCODE").
     * @param isAuto Whether to perform an automatic read.
     * @return DocumentReadResponse containing the results of the read operation.
     */
    public DocumentReadResponse read(String docType, boolean isAuto) {
        return executeWithDevice(openedDevice -> {
            for (DocumentReadStrategy strategy : strategies) {
                if (strategy.supports(docType)) {
                    // The strategy's read method will further interact with the JNA-wrapped FPHPSDevice.
                    return strategy.read(openedDevice, fastPassWebSocketHandler, isAuto);
                }
            }
            log.warn("No strategy found for document type: {}", docType);
            return null; // Or throw an exception
        });
    }

    /**
     * Closes the connection to the FPHPS device.
     * This directly calls the JNA-wrapped native library's close method.
     */
    public void closeDevice() {
        device.closeDevice();
    }

    /**
     * Scans a page using the FPHPS device.
     * This method configures device properties and uses the JNA-wrapped PageScanner to perform the scan.
     * @param lightType The type of light to use for scanning.
     * @return FPHPSImage containing the scanned image data.
     */
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
            // Setting device properties before scan, interacting with the JNA-wrapped device.
            openedDevice.getDeviceSetting().setDeviceProperties(deviceProperties);

            // Using the JNA-wrapped PageScanner to perform the scan operation.
            PageScanner scanner = new PageScanner(openedDevice);
            return scanner.scan(lightType);
        });
    }

}
