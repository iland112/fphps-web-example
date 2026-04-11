package com.smartcoreinc.fphps.example.fphps_web_example.Controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import com.smartcoreinc.fphps.dto.DeviceInfo;
import com.smartcoreinc.fphps.dto.DocumentReadResponse;
import com.smartcoreinc.fphps.dto.FPHPSImage;
import com.smartcoreinc.fphps.dto.properties.FPHPSDeviceProperties;
import com.smartcoreinc.fphps.example.fphps_web_example.Services.DevicePropertiesService;
import com.smartcoreinc.fphps.example.fphps_web_example.Services.FPHPSService;
import com.smartcoreinc.fphps.example.fphps_web_example.Services.PaApiSettingsService;
import com.smartcoreinc.fphps.example.fphps_web_example.Services.PassiveAuthenticationService;
import com.smartcoreinc.fphps.example.fphps_web_example.Services.ClientPaVerificationService;
import com.smartcoreinc.fphps.example.fphps_web_example.Services.FaceVerificationService;
import com.smartcoreinc.fphps.example.fphps_web_example.forms.DevSettingsForm;
import com.smartcoreinc.fphps.example.fphps_web_example.forms.EPassportSettingForm;
import com.smartcoreinc.fphps.example.fphps_web_example.forms.ScanForm;
import com.smartcoreinc.fphps.example.fphps_web_example.forms.SettingsForm;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.CertificateInfo;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.ParsedSODInfo;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.pa.PaLookupResponse;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.pa.ClientPaResult;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.pa.PaVerificationResponse;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.pa.PaVerificationResultWithData;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.face.FaceVerificationResponse;
import com.smartcoreinc.fphps.helpers.DocumentDataExporter;
import com.smartcoreinc.fphps.exception.DocumentExportException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;


@Slf4j
@Controller
@RequestMapping("/")
public class FPHPSController {

    private final FPHPSService fphpsService;
    private final DevicePropertiesService devicePropertiesService;
    private final PassiveAuthenticationService paService;
    private final ClientPaVerificationService clientPaService;
    private final FaceVerificationService faceService;
    private final PaApiSettingsService paApiSettingsService;

    @Value("${document-export.base-dir}")
    private String exportBaseDir;

    public FPHPSController(FPHPSService fphpsService, DevicePropertiesService devicePropertiesService,
                           PassiveAuthenticationService paService, ClientPaVerificationService clientPaService,
                           FaceVerificationService faceService,
                           PaApiSettingsService paApiSettingsService) {
        this.fphpsService = fphpsService;
        this.devicePropertiesService = devicePropertiesService;
        this.paService = paService;
        this.clientPaService = clientPaService;
        this.faceService = faceService;
        this.paApiSettingsService = paApiSettingsService;
    }

    @ModelAttribute("deviceProperties")
    public FPHPSDeviceProperties deviceProperties() {
        return devicePropertiesService.getProperties();
    }

    @GetMapping({"", "/"})
    public String index(Model model) {
        model.addAttribute("deviceAvailable", fphpsService.isDeviceAvailable());
        if (fphpsService.isDeviceAvailable()) {
            DeviceInfo deviceInfo = fphpsService.getDeviceInfo();
            model.addAttribute("device", deviceInfo);
        }

        FPHPSDeviceProperties deviceProperties = devicePropertiesService.getProperties();
        log.debug("Rendering index with properties: RF={}, IDCard={}, Barcode={}",
                  deviceProperties.getEnableRF(),
                  deviceProperties.getEnableIDCard(),
                  deviceProperties.getEnableBarcode());
        model.addAttribute("deviceProperties", deviceProperties);

        return "index";
    }

    @GetMapping("/home")
    public String getHomeContent(Model model) {
        model.addAttribute("deviceAvailable", fphpsService.isDeviceAvailable());
        if (fphpsService.isDeviceAvailable()) {
            DeviceInfo deviceInfo = fphpsService.getDeviceInfo();
            model.addAttribute("device", deviceInfo);
        }
        return "fragments/home_content";
    }

    @GetMapping("/device")
    public String getDevice(Model model) {
        model.addAttribute("deviceAvailable", fphpsService.isDeviceAvailable());
        if (fphpsService.isDeviceAvailable()) {
            DeviceInfo deviceInfo = fphpsService.getDeviceInfo();
            model.addAttribute("deviceInfo", deviceInfo);
        }
        return "fragments/device_info";
    }

    @GetMapping("/device-status")
    @ResponseBody
    public Map<String, Object> getDeviceStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("available", fphpsService.isDeviceAvailable());
        return result;
    }

    @PostMapping("/device-reconnect")
    @ResponseBody
    public Map<String, Object> reconnectDevice(Locale locale) {
        boolean isKo = locale != null && locale.getLanguage().equals("ko");
        Map<String, Object> result = new HashMap<>();
        try {
            fphpsService.reconnectDevice();
            result.put("success", true);
            result.put("message", isKo ? "장치가 성공적으로 연결되었습니다."
                                       : "Device connected successfully.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", isKo ? "장치를 찾을 수 없습니다. FastPass 장치를 연결한 후 다시 시도해 주세요."
                                       : "Device not found. Please connect the FastPass device and try again.");
        }
        return result;
    }

    /**
     * 서비스 재시작
     * WinSW 환경에서 System.exit(1)로 비정상 종료하면 자동 재시작됨
     * 개발 환경에서는 프로세스가 종료됨 (수동 재시작 필요)
     * 디바이스 작업이 멈춘 경우에도 강제 종료 가능 (Runtime.halt 폴백)
     */
    @PostMapping("/service-restart")
    @ResponseBody
    public Map<String, Object> restartService() {
        log.info("Service restart requested via web UI");
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Service is restarting...");

        fphpsService.forceShutdown();

        return result;
    }

    /**
     * 현재 디바이스 작업 상태 조회
     * IDLE: 대기 중, RUNNING: 작업 중, TIMED_OUT: 타임아웃 발생
     */
    @GetMapping("/device-operation-status")
    @ResponseBody
    public Map<String, Object> getDeviceOperationStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", fphpsService.getOperationStatus().name());
        result.put("available", fphpsService.isDeviceAvailable());
        return result;
    }

    /**
     * 현재 디바이스 작업 강제 취소
     * RF 통신이 멈춘 경우 디바이스를 강제 닫고 작업을 취소
     */
    @PostMapping("/device-operation-cancel")
    @ResponseBody
    public Map<String, Object> cancelDeviceOperation(Locale locale) {
        boolean isKo = locale != null && locale.getLanguage().equals("ko");
        Map<String, Object> result = new HashMap<>();
        boolean cancelled = fphpsService.cancelCurrentOperation();
        result.put("success", cancelled);
        if (cancelled) {
            result.put("message", isKo ? "작업이 취소되었습니다." : "Operation cancelled.");
        } else {
            result.put("message", isKo ? "취소할 작업이 없습니다." : "No operation to cancel.");
        }
        return result;
    }

    @GetMapping("/scan-page")
    public String showScanPage(Model model) {
        model.addAttribute("image", new FPHPSImage());
        return "fragments/scan_page";
    }

    @PostMapping("/scan-page")
    public String runScanPage(@ModelAttribute ScanForm scanForm, Model model) {
        FPHPSImage image = fphpsService.scanPage(scanForm.getLightType());
        model.addAttribute("image", image);
        return "fragments/scan_page";
    }

    @GetMapping("/device-setting")
    public String getDeviceSetting(Model model) {
        FPHPSDeviceProperties deviceProperties = devicePropertiesService.getProperties();
        SettingsForm settingsForm = SettingsForm.from(deviceProperties);
        model.addAttribute("settingsForm", settingsForm);
        return "settings_form";
    }

    @PostMapping("/device-setting")
    public String setDeviceSetting(@ModelAttribute SettingsForm settingsForm) {
        log.debug("Received settings from form: RF={}, IDCard={}, Barcode={}", 
                  settingsForm.isEnableRF(), 
                  settingsForm.isEnableIDCard(), 
                  settingsForm.isEnableBarcode());
        FPHPSDeviceProperties currentProperties = devicePropertiesService.getProperties();
        FPHPSDeviceProperties newProperties = SettingsForm.to(settingsForm, currentProperties);
        devicePropertiesService.setProperties(newProperties);
        return "redirect:/";
    }

    @GetMapping("/passport/manual-read")
    public String manualReadPost(@ModelAttribute EPassportSettingForm formData, Model model) {
        try {
            log.info("📖 Manual Read Started");
            DocumentReadResponse response = fphpsService.read("PASSPORT", false);

            // 응답 null 체크 및 로깅
            if (response == null) {
                log.warn("⚠ Manual Read: No response received - Passport may not have been detected");
                model.addAttribute("response", null);
                return "fragments/epassport_manual_read :: passport-information";
            }

            // VIZ 데이터 확인
            boolean hasVizData = response.getMrzInfo() != null;
            // Chip 데이터 확인
            boolean hasChipData = response.getEPassResults() != null;

            if (hasVizData && !hasChipData) {
                log.warn("⚠ Manual Read: VIZ data available but Chip read failed");
                log.info("→ Displaying VIZ data with chip failure warning");
            } else if (hasVizData && hasChipData) {
                log.info("✓ Manual Read: Both VIZ and Chip data successfully read");
            } else if (!hasVizData) {
                log.warn("⚠ Manual Read: No VIZ data - Passport not detected or positioned incorrectly");
            }

            // 이미지 데이터 확인 및 로깅
            log.info("📸 Image data in response:");
            log.info("  - VIZ Photo: {}", response.getVizPhotoImage() != null ? "✓ Present" : "✗ Absent");
            log.info("  - ePass Photo: {}", response.getEPassPhotoImage() != null ? "✓ Present" : "✗ Absent");
            log.info("  - MRZ Image: {}", response.getMrzImage() != null ? "✓ Present" : "✗ Absent");
            log.info("  - IR Image: {}", response.getIrImage() != null ? "✓ Present" : "✗ Absent");
            log.info("  - UV Image: {}", response.getUvImage() != null ? "✓ Present" : "✗ Absent");
            log.info("  - WH Image: {}", response.getWhImage() != null ? "✓ Present" : "✗ Absent");

            model.addAttribute("response", response);

            // ParsedSOD 정보 추출 및 모델에 추가 (Chip 데이터가 있을 때만)
            if (response.getParsedSOD() != null) {
                try {
                    ParsedSODInfo sodInfo = ParsedSODInfo.from(response.getParsedSOD());
                    model.addAttribute("parsedSODInfo", sodInfo);
                    log.debug("ParsedSOD information added to model");
                } catch (Exception e) {
                    log.warn("Failed to parse SOD information: {}", e.getMessage());
                }
            } else {
                log.debug("No ParsedSOD data available (chip read may have failed)");
            }

            return "fragments/epassport_manual_read :: passport-information";

        } catch (Exception e) {
            log.error("❌ Manual Read failed with exception: {}", e.getMessage(), e);
            // 예외 발생 시 빈 응답으로 처리하여 UI에 적절한 메시지 표시
            model.addAttribute("response", null);
            throw e; // GlobalExceptionHandler가 처리하도록 재던짐
        }
    }

    @GetMapping("/passport/auto-read")
    public String showAutoReadPage() {
        return "fragments/epassport_auto_read :: e-passport-auto-read";
    }

    @PostMapping("/passport/run-auto-read")
    @ResponseBody
    public void autoRead() {
        log.debug("autoRead() Started - triggering async read");
        // 비동기로 실행하여 HTTP 요청을 즉시 반환
        // 실제 읽기 결과는 WebSocket을 통해 클라이언트에 전달됨
        fphpsService.readAsync("PASSPORT");
        log.debug("autoRead() - async read triggered, returning immediately");
    }

    @GetMapping("/passport/get-sod-info")
    public String getAutoReadSODInfo(Model model) {
        DocumentReadResponse lastResponse = fphpsService.getLastReadResponse();

        if (lastResponse != null && lastResponse.getParsedSOD() != null) {
            ParsedSODInfo sodInfo = ParsedSODInfo.from(lastResponse.getParsedSOD());
            model.addAttribute("parsedSODInfo", sodInfo);
            log.debug("Retrieved SOD info from last auto-read: {}", sodInfo);
        } else {
            log.debug("No SOD data available from last auto-read");
            model.addAttribute("parsedSODInfo", null);
        }

        return "fragments/sod_information :: sodInformation";
    }

    /**
     * PA 검증 수행
     * 마지막 읽기 결과를 사용하여 API Gateway를 통해 PA API 호출
     * MRZ 데이터와 Face 이미지도 함께 반환
     */
    @PostMapping("/passport/verify-pa-v2")
    @ResponseBody
    public PaVerificationResultWithData verifyPassportPA() {
        log.debug("PA verification requested");

        DocumentReadResponse lastResponse = fphpsService.getLastReadResponse();
        if (lastResponse == null) {
            throw new PassiveAuthenticationService.PaVerificationException(
                "No passport data available. Please read passport first.");
        }

        // 디버그: 사용되는 데이터 확인
        if (lastResponse.getMrzInfo() != null) {
            log.info("PA using lastReadResponse: passportNumber={}, SOD size={}",
                lastResponse.getMrzInfo().getPassportNumber(),
                lastResponse.getSodDataBytes() != null ? lastResponse.getSodDataBytes().length : 0);
        }

        PaVerificationResponse paResult = paService.verifyFromDocumentResponse(lastResponse);
        return PaVerificationResultWithData.from(paResult, lastResponse);
    }

    /**
     * PA Lookup (간편 조회)
     * DSC Subject DN / SHA-256 Fingerprint 기반 Trust Chain 조회
     */
    @PostMapping("/passport/pa-lookup")
    @ResponseBody
    public Map<String, Object> paLookup() {
        log.debug("PA Lookup requested");

        DocumentReadResponse lastResponse = fphpsService.getLastReadResponse();
        if (lastResponse == null) {
            throw new PassiveAuthenticationService.PaVerificationException(
                "No passport data available. Please read passport first.");
        }

        PaLookupResponse lookupResponse = paService.paLookup(lastResponse);

        // 요청에 사용한 fingerprint를 함께 반환 (API가 null로 반환하므로 클라이언트 표시용)
        String requestFingerprint = null;
        if (lastResponse.getParsedSOD() != null) {
            try {
                ParsedSODInfo sodInfo = ParsedSODInfo.from(lastResponse.getParsedSOD());
                CertificateInfo dscCert = sodInfo.getDscCertificate();
                if (dscCert != null && dscCert.getSha256Fingerprint() != null
                    && !"N/A".equals(dscCert.getSha256Fingerprint())) {
                    requestFingerprint = dscCert.getSha256Fingerprint();
                }
            } catch (Exception e) {
                log.warn("Failed to extract fingerprint for response: {}", e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", lookupResponse.success());
        result.put("validation", lookupResponse.validation());
        result.put("message", lookupResponse.message());
        result.put("requestFingerprint", requestFingerprint);
        return result;
    }

    /**
     * 클라이언트 모드 PA 검증
     * 로컬에서 SOD 서명 + DG 해시 검증, Trust Chain만 PA Lookup API로 조회
     */
    @PostMapping("/passport/verify-pa-client")
    @ResponseBody
    public ClientPaResult verifyPassportPAClient() {
        log.debug("Client PA verification requested");

        DocumentReadResponse lastResponse = fphpsService.getLastReadResponse();
        if (lastResponse == null) {
            throw new PassiveAuthenticationService.PaVerificationException(
                "No passport data available. Please read passport first.");
        }

        return clientPaService.verify(lastResponse);
    }

    /**
     * PA API 서버 연결 상태 및 API Key 유효성 확인
     * 1단계: 서버 연결 확인 (GET /api/health - 인증 불필요)
     * 2단계: API Key 검증 (GET /api/pa/statistics - 인증 필요)
     */
    @GetMapping("/passport/pa-health")
    @ResponseBody
    public Map<String, Object> checkPaHealth() {
        Map<String, Object> result = new HashMap<>();

        // 1단계: 서버 연결 확인
        try {
            paService.healthCheck();
        } catch (Exception e) {
            log.warn("PA API health check failed: {}", e.getMessage());
            result.put("connected", false);
            result.put("apiKeyValid", false);
            result.put("message", "Server not reachable: " + e.getMessage());
            return result;
        }
        result.put("connected", true);

        // 2단계: API Key 유효성 검증
        String apiKey = paApiSettingsService.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            result.put("apiKeyValid", false);
            result.put("message", "API Key is not configured. Please enter an API Key and save.");
            return result;
        }

        try {
            paService.verifyApiKey();
            result.put("apiKeyValid", true);
        } catch (Exception e) {
            log.warn("PA API Key verification failed: {}", e.getMessage());
            result.put("apiKeyValid", false);
            String msg = e.getMessage();
            if (msg != null && (msg.contains("401") || msg.contains("Unauthorized"))) {
                result.put("message", "API Key authentication failed. The key may be invalid or expired.");
            } else if (msg != null && msg.contains("403")) {
                result.put("message", "API Key does not have sufficient permissions (pa:verify required).");
            } else {
                result.put("message", "API Key verification failed: " + msg);
            }
        }

        return result;
    }

    /**
     * PA 검증 결과를 Thymeleaf 프래그먼트로 렌더링
     */
    @GetMapping("/passport/pa-result")
    public String getPAVerificationResult(Model model) {
        // 이 엔드포인트는 HTMX 요청을 통해 호출되며,
        // 프론트엔드에서 JavaScript로 PA 검증 후 결과를 전달받아 사용합니다.
        // 현재는 프래그먼트만 반환하고, 실제 데이터는 HTMX 요청 시 함께 전달됩니다.
        return "fragments/pa_verification_result :: paResult";
    }

    /**
     * PA API 설정 조회
     */
    @GetMapping("/pa-api-settings")
    @ResponseBody
    public Map<String, String> getPaApiSettings() {
        Map<String, String> settings = new HashMap<>();
        settings.put("baseUrl", paApiSettingsService.getBaseUrl());
        settings.put("apiKey", paApiSettingsService.getApiKey());
        return settings;
    }

    /**
     * PA API 설정 저장 및 RestTemplate 재설정
     */
    @PostMapping("/pa-api-settings")
    @ResponseBody
    public Map<String, Object> savePaApiSettings(@RequestBody Map<String, String> settings) {
        Map<String, Object> result = new HashMap<>();
        try {
            paApiSettingsService.updateSettings(settings.get("baseUrl"), settings.get("apiKey"));
            result.put("success", true);
            result.put("message", "PA API settings saved successfully");
        } catch (Exception e) {
            log.error("Failed to save PA API settings", e);
            result.put("success", false);
            result.put("message", "Failed to save: " + e.getMessage());
        }
        return result;
    }

    /**
     * CA 인증서 업로드
     */
    @PostMapping("/pa-api-settings/ca-cert")
    @ResponseBody
    public Map<String, Object> uploadCaCert(@RequestParam("certFile") MultipartFile certFile) {
        Map<String, Object> result = new HashMap<>();
        try {
            String info = paApiSettingsService.uploadCaCertificate(certFile);
            result.put("success", true);
            result.put("message", "CA certificate uploaded successfully");
            result.put("certInfo", info);
        } catch (Exception e) {
            log.error("Failed to upload CA certificate: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Failed to upload: " + e.getMessage());
        }
        return result;
    }

    /**
     * CA 인증서 정보 조회
     */
    @GetMapping("/pa-api-settings/ca-cert")
    @ResponseBody
    public Map<String, Object> getCaCertInfo() {
        Map<String, Object> result = new HashMap<>();
        String info = paApiSettingsService.getCaCertificateInfo();
        result.put("exists", info != null);
        result.put("certInfo", info);
        return result;
    }

    /**
     * CA 인증서 삭제
     */
    @PostMapping("/pa-api-settings/ca-cert/delete")
    @ResponseBody
    public Map<String, Object> deleteCaCert() {
        Map<String, Object> result = new HashMap<>();
        try {
            paApiSettingsService.deleteCaCertificate();
            result.put("success", true);
            result.put("message", "CA certificate deleted");
        } catch (Exception e) {
            log.error("Failed to delete CA certificate: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Failed to delete: " + e.getMessage());
        }
        return result;
    }

    @GetMapping("/idcard/manual-read")
    public String idCardManualRead(Model model) {
        DocumentReadResponse response = fphpsService.read("IDCARD", false);
        model.addAttribute("response", response);
        return "fragments/idcard_manual_read :: id-card-information";
    }

    @GetMapping("/idcard/auto-read")
    public String showIDCardAutoReadPage() {
        return "fragments/idcard_auto_read :: id-card-auto-read";
    }

    @PostMapping("/idcard/run-auto-read")
    @ResponseBody
    public void idCardAutoRead() {
        log.debug("idCardAutoRead() Started!!");
        fphpsService.read("IDCARD", true);
        log.debug("idCardAutoRead() Ended!!");
    }

    @GetMapping("/barcode/manual-read")
    public String barcodeManualRead(Model model) {
        DocumentReadResponse response = fphpsService.read("BARCODE", false);
        model.addAttribute("response", response);
        return "fragments/barcode_manual_read :: barcode-information";
    }

    @GetMapping("/barcode/auto-read")
    public String showBarcodeAutoReadPage() {
        return "fragments/barcode_auto_read :: barcode-auto-read";
    }

    @PostMapping("/barcode/run-auto-read")
    @ResponseBody
    public void barcodeAutoRead() {
        log.debug("barcodeAutoRead() Started!!");
        fphpsService.read("BARCODE", true);
        log.debug("barcodeAutoRead() Ended!!");
    }

    @GetMapping("/test")
    public String deviceSetting(Model model) {
        model.addAttribute("settingsForm", new DevSettingsForm());
        return "device_setting_form";
    }

    /**
     * Face Verification 수행
     * 마지막 읽기 결과를 사용하여 InsightFace API로 얼굴 검증 수행
     * Document photo (VIZ)와 Chip photo (DG2)를 비교
     */
    @PostMapping("/passport/verify-face")
    @ResponseBody
    public FaceVerificationResponse verifyFace() {
        log.debug("Face verification requested");

        DocumentReadResponse lastResponse = fphpsService.getLastReadResponse();
        if (lastResponse == null) {
            throw new FaceVerificationService.FaceVerificationException(
                "No passport data available. Please read passport first.");
        }

        return faceService.verifyFromDocumentResponse(lastResponse);
    }

    /**
     * 여권 데이터 내보내기
     * 마지막 읽기 결과를 파일 시스템에 저장
     * - 이미지 파일 (VIZ, ePass, MRZ, IR, UV, WH)
     * - SOD 및 Data Group 바이너리 파일
     * - MRZ 텍스트 파일
     *
     * @return 내보내기 결과 (성공 시 저장 경로, 실패 시 에러 메시지)
     */
    @PostMapping(value = "/passport/export-data", produces = "application/json")
    @ResponseBody
    public Map<String, Object> exportPassportData() {
        log.debug("Document data export requested");

        Map<String, Object> response = new HashMap<>();

        try {
            // 마지막 읽기 결과 확인
            DocumentReadResponse lastResponse = fphpsService.getLastReadResponse();
            if (lastResponse == null) {
                response.put("success", false);
                response.put("message", "No passport data available. Please read passport first.");
                return response;
            }

            // 여권 번호 확인
            String passportNumber = null;
            if (lastResponse.getMrzInfo() != null) {
                passportNumber = lastResponse.getMrzInfo().getPassportNumber();
            }

            if (passportNumber == null || passportNumber.trim().isEmpty()) {
                log.warn("Passport number is missing, using timestamp-based folder name");
            }

            // 데이터 내보내기
            Path exportPath = DocumentDataExporter.exportToFolder(lastResponse, exportBaseDir);

            log.info("Document data exported successfully to: {}", exportPath);

            response.put("success", true);
            response.put("message", "Document data exported successfully");
            response.put("exportPath", exportPath.toString());
            response.put("passportNumber", passportNumber != null ? passportNumber : "UNKNOWN");

        } catch (DocumentExportException e) {
            log.error("Failed to export document data", e);
            response.put("success", false);
            response.put("message", "Export failed: " + e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error during document data export", e);
            response.put("success", false);
            response.put("message", "Unexpected error: " + e.getMessage());
        }

        return response;
    }

}
