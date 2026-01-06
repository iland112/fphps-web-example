package com.smartcoreinc.fphps.example.fphps_web_example.Services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import com.smartcoreinc.fphps.dto.DeviceInfo;
import com.smartcoreinc.fphps.dto.DocumentReadResponse;
import com.smartcoreinc.fphps.dto.FPHPSImage;
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
public class FPHPSService {

    private final FPHPSDeviceManager deviceManager;
    private final FastPassWebSocketHandler fastPassWebSocketHandler;
    private final List<DocumentReadStrategy> strategies;
    private final DevicePropertiesService devicePropertiesService;
    private FPHPSDevice device;

    // Auto-read의 마지막 읽기 결과 저장
    private volatile DocumentReadResponse lastReadResponse;

    public FPHPSService(FastPassWebSocketHandler fastPassWebSocketHandler, List<DocumentReadStrategy> strategies, DevicePropertiesService devicePropertiesService) {
        this.fastPassWebSocketHandler = fastPassWebSocketHandler;
        this.strategies = strategies;
        this.devicePropertiesService = devicePropertiesService;
        this.deviceManager = FPHPSDeviceManager.getInstance();

        // Auto Read 완료 시 결과를 lastReadResponse에 저장하는 콜백 등록
        this.fastPassWebSocketHandler.setOnReadCompleteCallback(this::saveAutoReadResponse);
        try {
            initDevices();
            // DB에 저장된 설정이 있는지 확인
            FPHPSDeviceProperties savedProperties = this.devicePropertiesService.getProperties();
            boolean hasSavedSettings = savedProperties != null &&
                (savedProperties.getBatchModeProperties() != null ||
                 savedProperties.getEPassportDGProperties() != null ||
                 savedProperties.getEPassportAuthProperties() != null);

            try {
                this.device.openDevice();
                if (hasSavedSettings) {
                    // DB에 저장된 설정이 있으면 디바이스에 적용
                    this.device.setDeviceProperties(savedProperties);
                    log.info("Applied saved settings from database to device.");
                } else {
                    // DB에 저장된 설정이 없으면 디바이스에서 가져와서 저장
                    FPHPSDeviceProperties initialProperties = this.device.getDeviceProperties();
                    this.devicePropertiesService.setProperties(initialProperties);
                    log.info("No saved settings found. Loaded initial properties from device and saved to database.");
                }
            } finally {
                if (this.device.isDeviceOpened()) {
                    this.device.closeDevice();
                }
            }
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

    private synchronized <R> R executeWithDevice(Function<FPHPSDevice, R> action) {
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
            if (device.isDeviceOpened()) {
                device.closeDevice();
            }
        }
    }

    private synchronized void executeWithDevice(Consumer<FPHPSDevice> action) {
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

    public DocumentReadResponse read(String docType, boolean isAuto) {
        log.debug("read() called: docType={}, isAuto={}", docType, isAuto);

        DocumentReadResponse response = executeWithDevice(openedDevice -> {
            for (DocumentReadStrategy strategy : strategies) {
                if (strategy.supports(docType)) {
                    return strategy.read(openedDevice, fastPassWebSocketHandler, isAuto);
                }
            }
            log.warn("No strategy found for document type: {}", docType);
            return null;
        });

        // 읽기 결과 저장 (Manual/Auto 모두)
        if (response != null) {
            this.lastReadResponse = response;
            // 디버그: 저장된 데이터 확인
            if (response.getMrzInfo() != null) {
                log.info("lastReadResponse saved: passportNumber={}, SOD size={}",
                    response.getMrzInfo().getPassportNumber(),
                    response.getSodDataBytes() != null ? response.getSodDataBytes().length : 0);
            } else {
                log.warn("lastReadResponse saved but MrzInfo is null");
            }
        } else {
            log.warn("read() returned null response, lastReadResponse not updated");
        }

        return response;
    }

    /**
     * 비동기 Auto Read 실행
     * HTTP 요청은 즉시 반환하고, 실제 읽기는 별도 스레드에서 수행
     * 결과는 WebSocket을 통해 클라이언트에 전달됨
     */
    @Async
    public CompletableFuture<DocumentReadResponse> readAsync(String docType) {
        log.debug("readAsync() started: docType={}", docType);
        try {
            DocumentReadResponse response = read(docType, true);
            log.debug("readAsync() completed");
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            log.error("readAsync() failed: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Auto-read의 마지막 읽기 결과 반환
     * @return 마지막 DocumentReadResponse 또는 null
     */
    public DocumentReadResponse getLastReadResponse() {
        return lastReadResponse;
    }

    /**
     * Auto Read 완료 시 호출되는 콜백 메서드
     * WebSocketHandler에서 FPHPS_EV_EPASS_READ_DONE 이벤트 수신 시 호출됨
     * @param response Auto Read 결과
     */
    private void saveAutoReadResponse(DocumentReadResponse response) {
        if (response != null) {
            this.lastReadResponse = response;
            if (response.getMrzInfo() != null) {
                log.info("Auto Read response saved via callback: passportNumber={}, SOD size={}",
                    response.getMrzInfo().getPassportNumber(),
                    response.getSodDataBytes() != null ? response.getSodDataBytes().length : 0);
            } else {
                log.info("Auto Read response saved via callback (MrzInfo is null)");
            }
        } else {
            log.warn("saveAutoReadResponse called with null response");
        }
    }

    /**
     * 저장된 마지막 읽기 결과 초기화
     */
    public void clearLastReadResponse() {
        this.lastReadResponse = null;
        log.debug("Cleared last read response");
    }

    public void closeDevice() {
        device.closeDevice();
    }

    public FPHPSImage scanPage(int lightType) {
        return executeWithDevice(openedDevice -> {
            PageScanner scanner = new PageScanner(openedDevice);
            return scanner.scan(lightType);
        });
    }
}