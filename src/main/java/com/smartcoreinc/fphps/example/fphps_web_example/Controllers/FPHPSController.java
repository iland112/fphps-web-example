package com.smartcoreinc.fphps.example.fphps_web_example.Controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import com.smartcoreinc.fphps.dto.DeviceInfo;
import com.smartcoreinc.fphps.dto.DocumentReadResponse;
import com.smartcoreinc.fphps.dto.FPHPSImage;
import com.smartcoreinc.fphps.dto.properties.FPHPSDeviceProperties;
import com.smartcoreinc.fphps.example.fphps_web_example.Services.DevicePropertiesService;
import com.smartcoreinc.fphps.example.fphps_web_example.Services.FPHPSService;
import com.smartcoreinc.fphps.example.fphps_web_example.forms.DevSettingsForm;
import com.smartcoreinc.fphps.example.fphps_web_example.forms.EPassportSettingForm;
import com.smartcoreinc.fphps.example.fphps_web_example.forms.ScanForm;
import com.smartcoreinc.fphps.example.fphps_web_example.forms.SettingsForm;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.ParsedSODInfo;


import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Slf4j
@Controller
@RequestMapping("/fphps")
public class FPHPSController {

    private final FPHPSService fphpsService;
    private final DevicePropertiesService devicePropertiesService;

    public FPHPSController(FPHPSService fphpsService, DevicePropertiesService devicePropertiesService) {
        this.fphpsService = fphpsService;
        this.devicePropertiesService = devicePropertiesService;
    }

    @ModelAttribute("deviceProperties")
    public FPHPSDeviceProperties deviceProperties() {
        return devicePropertiesService.getProperties();
    }

    @GetMapping({"", "/"})  
    public String index(Model model) {
        DeviceInfo deviceInfo = fphpsService.getDeviceInfo();
        model.addAttribute("device", deviceInfo);
        
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
        DeviceInfo deviceInfo = fphpsService.getDeviceInfo();
        model.addAttribute("device", deviceInfo);
        return "fragments/home_content";
    }

    @GetMapping("/device")
    public String getDevice(Model model) {
        DeviceInfo deviceInfo = fphpsService.getDeviceInfo();
        model.addAttribute("deviceInfo", deviceInfo);
        return "fragments/device_info";
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
        return "redirect:/fphps";
    }

    @GetMapping("/passport/manual-read")
    public String manualReadPost(@ModelAttribute EPassportSettingForm formData, Model model) {
        DocumentReadResponse response = fphpsService.read("PASSPORT", false);
        model.addAttribute("response", response);

        // ParsedSOD 정보 추출 및 모델에 추가
        if (response != null && response.getParsedSOD() != null) {
            ParsedSODInfo sodInfo = ParsedSODInfo.from(response.getParsedSOD());
            model.addAttribute("parsedSODInfo", sodInfo);
            log.debug("ParsedSOD information added to model: {}", sodInfo);
        } else {
            log.debug("No ParsedSOD data available in response");
        }

        return "fragments/epassport_manual_read";
    }

    @GetMapping("/passport/auto-read")
    public String showAutoReadPage() {
        return "fragments/epassport_auto_read";
    }

    @PostMapping("/passport/run-auto-read")
    @ResponseBody
    public void autoRead() {
        log.debug("autoRead() Started!!");
        fphpsService.read("PASSPORT", true);
        log.debug("autoRead() Ended!!");
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

    @GetMapping("/idcard/manual-read")
    public String idCardManualRead(Model model) {
        DocumentReadResponse response = fphpsService.read("IDCARD", false);
        model.addAttribute("response", response);
        return "fragments/idcard_manual_read";
    }

    @GetMapping("/idcard/auto-read")
    public String showIDCardAutoReadPage() {
        return "fragments/idcard_auto_read";
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
        return "fragments/barcode_manual_read";
    }

    @GetMapping("/barcode/auto-read")
    public String showBarcodeAutoReadPage() {
        return "fragments/barcode_auto_read";
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


}
