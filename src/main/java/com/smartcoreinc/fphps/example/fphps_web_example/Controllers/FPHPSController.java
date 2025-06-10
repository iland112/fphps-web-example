package com.smartcoreinc.fphps.example.fphps_web_example.Controllers;

import org.springframework.web.bind.annotation.RequestMapping;

import com.smartcoreinc.fphps.dto.DeviceInfo;
import com.smartcoreinc.fphps.dto.DocumentReadResponse;
import com.smartcoreinc.fphps.dto.FPHPSImage;
import com.smartcoreinc.fphps.dto.properties.DeviceProperties;
import com.smartcoreinc.fphps.example.fphps_web_example.Services.FPHPSService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


@Slf4j
@Controller
@RequestMapping("/fphps")
public class FPHPSController {

    private final FPHPSService fphpsService;

    public FPHPSController(FPHPSService fphpsService) {
        this.fphpsService = fphpsService;
    }

    @GetMapping("")  
    public String index(Model model) {
        DeviceInfo deviceInfo = fphpsService.getDeviceInfo();
        model.addAttribute("device", deviceInfo);
        return "index";
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
        DeviceProperties deviceProperties = fphpsService.getCurrentDeviceProperties();
        SettingsForm settingsForm = SettingsForm.from(deviceProperties);
        model.addAttribute("settingsForm", settingsForm);
        return "settings_form";
    }

    @PostMapping("/device-setting")
    public String setDeviceSetting(@ModelAttribute SettingsForm settingsForm) {
        
        DeviceProperties deviceProperties = SettingsForm.to(settingsForm);
        fphpsService.setDeviceProperties(deviceProperties);

        return "redirect:/fphps";
    }


    @GetMapping("/passport/manual-read")
    public String manualRead(Model model) {
        try {
            DocumentReadResponse response = fphpsService.read("PASSPORT", false);
            // Logger.debug("photo base64: {}", responseDto.getPhotoImage());
            // log.debug(response.getEPassResults().toString());
            model.addAttribute("response", response);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return "fragments/epassport_manual_read";
    }

    @GetMapping("/passport/auto-read")
    public String showAutoReadPage() {
        return "fragments/epassport_auto_read";
    }

    @GetMapping("/passport/run-auto-read")
    public void autoRead() {
        log.debug("autoRead() Started!!");
        try {
            fphpsService.read("PASSPORT", true);
        } catch (com.smartcoreinc.fphps.exception.FPHPSException e) {
            log.error(e.getMessage());
        } finally {
            fphpsService.closeDevice();
            log.debug("Device was closed");
        }
        log.debug("autoRead() Ended!!");
    }

    @GetMapping("/idcard/manual-read")
    public String idCardManualRead(Model model) {
        try {
            DocumentReadResponse response = fphpsService.read("IDCARD", false);
            // Logger.debug("photo base64: {}", responseDto.getPhotoImage());
            // log.debug(response.getEPassResults().toString());
            model.addAttribute("response", response);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return "fragments/idcard_manual_read";
    }

    @GetMapping("/idcard/auto-read")
    public String showIDCardAutoReadPage() {
        return "fragments/idcard_auto_read";
    }

    @GetMapping("/idcard/run-auto-read")
    public void idCardAutoRead() {
        log.debug("idCardAutoRead() Started!!");
        try {
            fphpsService.read("IDCARD", true);
        } catch (com.smartcoreinc.fphps.exception.FPHPSException e) {
            log.error(e.getMessage());
        } finally {
            fphpsService.closeDevice();
            log.debug("Device was closed");
        }
        log.debug("idCardAutoRead() Ended!!");
    }

    @GetMapping("/barcode/manual-read")
    public String barcodeManualRead(Model model) {
        try {
            DocumentReadResponse response = fphpsService.read("BARCODE", false);
            // log.debug(response.getBarCode().toString());
            // log.debug(response.getWhCropImage().getImageData());
            model.addAttribute("response", response);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return "fragments/barcode_manual_read";
    }

    @GetMapping("/barcode/auto-read")
    public String showBarcodeAutoReadPage() {
        return "fragments/barcode_auto_read";
    }

    @GetMapping("/barcode/run-auto-read")
    public void barcodeAutoRead() {
        log.debug("barcodeAutoRead() Started!!");
        try {
            fphpsService.read("BARCODE", true);
        } catch (com.smartcoreinc.fphps.exception.FPHPSException e) {
            log.error(e.getMessage());
        } finally {
            fphpsService.closeDevice();
            log.debug("Device was closed");
        }
        log.debug("barcodeAutoRead() Ended!!");
    }
}
