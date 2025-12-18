package com.smartcoreinc.fphps.example.fphps_web_example.advice;

import com.smartcoreinc.fphps.example.fphps_web_example.exceptions.DeviceOperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DeviceOperationException.class)
    public String handleDeviceOperationException(DeviceOperationException ex, Model model) {
        log.error("Device operation failed: {}", ex.getMessage(), ex);
        model.addAttribute("errorMessage", ex.getMessage());
        return "fragments/error_display"; // This will map to src/main/resources/templates/fragments/error_display.html
    }

    // You can add more @ExceptionHandler methods for other types of exceptions if needed
    // @ExceptionHandler(Exception.class)
    // public String handleGenericException(Exception ex, Model model) {
    //     log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
    //     model.addAttribute("errorMessage", "An unexpected error occurred: " + ex.getMessage());
    //     return "fragments/error_display";
    // }
}
