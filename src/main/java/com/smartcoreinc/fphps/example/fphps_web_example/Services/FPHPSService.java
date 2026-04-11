package com.smartcoreinc.fphps.example.fphps_web_example.Services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
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

    /** 디바이스 작업 상태 */
    public enum DeviceOperationStatus {
        IDLE,       // 대기 중
        RUNNING,    // 작업 진행 중
        TIMED_OUT   // 타임아웃 발생 (스레드가 아직 멈춰있을 수 있음)
    }

    /** 디바이스 작업 타임아웃 (초) - RF 통신 응답 대기 최대 시간 */
    private static final int DEVICE_OPERATION_TIMEOUT_SECONDS = 60;
    /** 디바이스 락 획득 대기 시간 (초) */
    private static final int LOCK_ACQUIRE_TIMEOUT_SECONDS = 5;

    private final FPHPSDeviceManager deviceManager;
    private final FastPassWebSocketHandler fastPassWebSocketHandler;
    private final List<DocumentReadStrategy> strategies;
    private final DevicePropertiesService devicePropertiesService;
    private FPHPSDevice device;
    private boolean deviceAvailable = false;

    // Auto-read의 마지막 읽기 결과 저장
    private volatile DocumentReadResponse lastReadResponse;

    // synchronized 대신 ReentrantLock 사용 (타임아웃 지원)
    private final ReentrantLock deviceLock = new ReentrantLock();
    // 디바이스 작업 전용 단일 스레드 (타임아웃 제어용, 멈추면 교체 가능)
    private volatile ExecutorService deviceExecutor = createDeviceExecutor();
    // 현재 작업 상태
    private volatile DeviceOperationStatus operationStatus = DeviceOperationStatus.IDLE;
    // 현재 실행 중인 Future (강제 취소용)
    private volatile Future<?> currentOperation;
    // 현재 작업을 실행 중인 스레드 (interrupt용)
    private volatile Thread operationThread;

    private static ExecutorService createDeviceExecutor() {
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "DeviceOp");
            t.setDaemon(true);
            return t;
        });
    }

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
            this.deviceAvailable = true;
            log.info("FPHPS device initialized successfully.");
        } catch (com.smartcoreinc.fphps.exception.FPHPSException e) {
            log.warn("FPHPS device not available: {}. Web application will start without device.", e.getMessage());
            this.deviceAvailable = false;
        } catch (Exception e) {
            log.warn("FPHPS device initialization failed: {}. Web application will start without device.", e.getMessage());
            this.deviceAvailable = false;
        }
    }

    /**
     * 디바이스 연결 상태 확인
     */
    public boolean isDeviceAvailable() {
        return deviceAvailable;
    }

    /**
     * 디바이스 재연결 시도
     */
    public void reconnectDevice() {
        try {
            initDevices();
            FPHPSDeviceProperties savedProperties = this.devicePropertiesService.getProperties();
            boolean hasSavedSettings = savedProperties != null &&
                (savedProperties.getBatchModeProperties() != null ||
                 savedProperties.getEPassportDGProperties() != null ||
                 savedProperties.getEPassportAuthProperties() != null);

            try {
                this.device.openDevice();
                if (hasSavedSettings) {
                    this.device.setDeviceProperties(savedProperties);
                    log.info("Applied saved settings from database to reconnected device.");
                } else {
                    FPHPSDeviceProperties initialProperties = this.device.getDeviceProperties();
                    this.devicePropertiesService.setProperties(initialProperties);
                    log.info("Loaded initial properties from reconnected device.");
                }
            } finally {
                if (this.device.isDeviceOpened()) {
                    this.device.closeDevice();
                }
            }
            this.deviceAvailable = true;
            log.info("FPHPS device reconnected successfully.");
        } catch (Exception e) {
            log.warn("FPHPS device reconnect failed: {}", e.getMessage());
            this.deviceAvailable = false;
            throw new DeviceOperationException("Device not found. Please connect the FastPass device and try again.");
        }
    }

    private void initDevices() {
        this.deviceManager.enumerateDevices();
        this.device = this.deviceManager.getDevice();
    }

    public DeviceInfo getDeviceInfo() {
        if (!deviceAvailable || device == null) {
            throw new DeviceOperationException("Device not connected. Please connect the FastPass device.");
        }
        return device.getDeviceInfo();
    }

    /**
     * 디바이스 작업을 타임아웃 보호와 함께 실행.
     * - ReentrantLock으로 동시 접근 방지 (tryLock 타임아웃)
     * - 별도 스레드에서 실행하여 Future.get(timeout)으로 타임아웃 강제
     * - 타임아웃 시 디바이스 강제 닫기 및 스레드 인터럽트
     */
    private <R> R executeWithDevice(Function<FPHPSDevice, R> action) {
        if (!deviceAvailable || device == null) {
            throw new DeviceOperationException("Device not connected. Please connect the FastPass device and try again.");
        }

        // 1. 락 획득 (다른 작업이 진행 중이면 짧게 대기 후 실패)
        boolean locked;
        try {
            locked = deviceLock.tryLock(LOCK_ACQUIRE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DeviceOperationException("Device operation interrupted while waiting.");
        }
        if (!locked) {
            if (operationStatus == DeviceOperationStatus.TIMED_OUT) {
                throw new DeviceOperationException(
                    "A previous device operation has timed out and the device may be unresponsive. " +
                    "Please cancel the operation or restart the service.");
            }
            throw new DeviceOperationException(
                "Another device operation is in progress. Please wait for it to complete.");
        }

        // 2. 별도 스레드에서 디바이스 작업 실행 (타임아웃 제어)
        operationStatus = DeviceOperationStatus.RUNNING;
        try {
            Future<R> future = deviceExecutor.submit(() -> {
                operationThread = Thread.currentThread();
                try {
                    device.openDevice();
                    try {
                        return action.apply(device);
                    } catch (com.smartcoreinc.fphps.exception.FPHPSException e) {
                        log.error("FPHPS device operation failed: {}", e.getMessage(), e);
                        throw new DeviceOperationException("FPHPS device error: " + e.getMessage(), e);
                    } catch (Exception e) {
                        log.error("An unexpected error during device operation: {}", e.getMessage(), e);
                        throw new DeviceOperationException(
                            "An unexpected error occurred during device operation: " + e.getMessage(), e);
                    }
                } finally {
                    try {
                        if (device.isDeviceOpened()) {
                            device.closeDevice();
                        }
                    } catch (Exception e) {
                        log.warn("Failed to close device in finally block: {}", e.getMessage());
                    }
                    operationThread = null;
                }
            });
            currentOperation = future;

            // 3. 타임아웃 대기
            try {
                R result = future.get(DEVICE_OPERATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                operationStatus = DeviceOperationStatus.IDLE;
                return result;
            } catch (TimeoutException e) {
                operationStatus = DeviceOperationStatus.TIMED_OUT;
                log.error("Device operation timed out after {} seconds. " +
                          "RF communication may be stuck.", DEVICE_OPERATION_TIMEOUT_SECONDS);
                // 타임아웃 시 강제 정리
                forceAbortCurrentOperation();
                throw new DeviceOperationException(
                    "Device operation timed out (" + DEVICE_OPERATION_TIMEOUT_SECONDS +
                    "s). The RF reader may be unresponsive. Please try again or restart the service.");
            } catch (ExecutionException e) {
                operationStatus = DeviceOperationStatus.IDLE;
                Throwable cause = e.getCause();
                if (cause instanceof DeviceOperationException) {
                    throw (DeviceOperationException) cause;
                }
                throw new DeviceOperationException("Device operation failed: " + cause.getMessage(), cause);
            } catch (InterruptedException e) {
                operationStatus = DeviceOperationStatus.IDLE;
                Thread.currentThread().interrupt();
                future.cancel(true);
                throw new DeviceOperationException("Device operation was interrupted.");
            }
        } finally {
            currentOperation = null;
            deviceLock.unlock();
        }
    }

    private void executeWithDeviceVoid(Consumer<FPHPSDevice> action) {
        executeWithDevice(device -> {
            action.accept(device);
            return null;
        });
    }

    /**
     * 현재 진행 중인 디바이스 작업 강제 취소.
     * 타임아웃 또는 사용자 취소 시 호출.
     * - Future.cancel(true)로 인터럽트 시도
     * - device.closeDevice()로 네이티브 레벨 중단 시도
     */
    private void forceAbortCurrentOperation() {
        Future<?> op = currentOperation;
        if (op != null) {
            op.cancel(true);
        }
        Thread t = operationThread;
        if (t != null) {
            t.interrupt();
        }
        // 디바이스 강제 닫기 (네이티브 라이브러리가 블로킹 해제될 수 있음)
        try {
            if (device != null && device.isDeviceOpened()) {
                log.warn("Force-closing device to abort stuck operation");
                device.closeDevice();
            }
        } catch (Exception e) {
            log.warn("Failed to force-close device: {}", e.getMessage());
        }
    }

    /**
     * 현재 디바이스 작업 상태 조회
     */
    public DeviceOperationStatus getOperationStatus() {
        return operationStatus;
    }

    /**
     * 사용자 또는 시스템에 의한 강제 작업 취소.
     * UI에서 "Cancel Operation" 버튼 클릭 시 호출.
     * @return 취소 시도 결과
     */
    public boolean cancelCurrentOperation() {
        if (operationStatus == DeviceOperationStatus.IDLE) {
            return false; // 취소할 작업 없음
        }
        log.warn("Cancel requested by user - aborting current device operation");
        forceAbortCurrentOperation();

        // deviceExecutor가 타임아웃 후에도 멈춰있을 수 있으므로
        // 새 executor로 교체하여 다음 작업이 가능하도록 함
        resetDeviceExecutorIfNeeded();
        operationStatus = DeviceOperationStatus.IDLE;
        return true;
    }

    /**
     * 타임아웃으로 멈춘 executor를 새 것으로 교체.
     * 기존 스레드는 daemon이므로 JVM 종료 시 정리됨.
     * 새 executor로 교체하여 다음 작업이 즉시 가능하도록 함.
     */
    private void resetDeviceExecutorIfNeeded() {
        ExecutorService oldExecutor = deviceExecutor;
        deviceExecutor = createDeviceExecutor();
        // 기존 executor는 shutdownNow로 정리 시도 (daemon 스레드이므로 멈춘 채로 남아도 JVM에는 영향 없음)
        oldExecutor.shutdownNow();
        log.info("Device executor replaced with a fresh instance.");
    }

    /**
     * 서비스 강제 종료 (서비스 재시작용).
     * 일반 System.exit(1)이 synchronized lock에 의해 차단될 수 있으므로
     * Runtime.halt()를 폴백으로 사용.
     */
    public void forceShutdown() {
        log.warn("Force shutdown requested");
        // 진행 중인 작업 정리 시도
        forceAbortCurrentOperation();

        new Thread(() -> {
            try {
                Thread.sleep(1500); // 응답 전송 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log.info("Shutting down for restart (exit code 1)...");
            try {
                System.exit(1);
            } catch (Exception e) {
                // System.exit가 shutdown hook에서 멈출 경우 강제 종료
                log.error("System.exit blocked, using Runtime.halt(1)");
                Runtime.getRuntime().halt(1);
            }
        }, "ShutdownThread").start();

        // System.exit이 5초 내 완료되지 않으면 halt로 강제 종료
        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            log.error("Graceful shutdown timed out. Forcing halt.");
            Runtime.getRuntime().halt(1);
        }, "ShutdownWatchdog").start();
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
            if (response.getMrzInfo() != null && response.getMrzInfo().getPassportNumber() != null) {
                log.info("✓ Read response saved - Passport: {}, SOD: {} bytes, Mode: {}",
                    response.getMrzInfo().getPassportNumber(),
                    response.getSodDataBytes() != null ? response.getSodDataBytes().length : 0,
                    isAuto ? "AUTO" : "MANUAL");
            } else {
                log.warn("⚠ Read response saved but NO MRZ DATA - Mode: {}", isAuto ? "AUTO" : "MANUAL");
            }
        } else {
            if (isAuto) {
                log.debug("Auto Read: read() returned null (expected - data will be retrieved via callback)");
            } else {
                log.warn("⚠ Manual Read: read() returned null response - Passport may not have been detected");
            }
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
            if (response.getMrzInfo() != null && response.getMrzInfo().getPassportNumber() != null) {
                log.info("✓ Auto Read callback saved - Passport: {}, SOD: {} bytes",
                    response.getMrzInfo().getPassportNumber(),
                    response.getSodDataBytes() != null ? response.getSodDataBytes().length : 0);
            } else {
                log.warn("⚠ Auto Read callback saved but NO MRZ DATA - Passport was not detected during Auto Read");
            }
        } else {
            log.warn("⚠ Auto Read callback called with NULL response - No passport data available");
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