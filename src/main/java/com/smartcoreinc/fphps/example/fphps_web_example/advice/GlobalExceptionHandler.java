package com.smartcoreinc.fphps.example.fphps_web_example.advice;

import com.smartcoreinc.fphps.example.fphps_web_example.Services.PassiveAuthenticationService;
import com.smartcoreinc.fphps.example.fphps_web_example.exceptions.DeviceOperationException;
import com.smartcoreinc.fphps.exception.DeviceNotFoundException;
import com.smartcoreinc.fphps.exception.DeviceNotOpenedException;
import com.smartcoreinc.fphps.exception.NativeLibraryException;
import com.smartcoreinc.fphps.exception.SODVerificationException;
import com.smartcoreinc.fphps.exception.FPHPSException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * HTMX 요청인지 확인
     */
    private boolean isHtmxRequest(HttpServletRequest request) {
        return "true".equals(request.getHeader("HX-Request"));
    }

    /**
     * JSON/REST API 요청인지 확인
     * - Accept 헤더가 application/json인 경우
     * - Content-Type이 application/json인 경우
     * - XHR 요청이면서 HTMX가 아닌 경우
     */
    private boolean isJsonRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String contentType = request.getContentType();
        String xRequestedWith = request.getHeader("X-Requested-With");

        boolean acceptsJson = accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE);
        boolean sendsJson = contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE);
        boolean isXhr = "XMLHttpRequest".equals(xRequestedWith);

        return acceptsJson || sendsJson || (isXhr && !isHtmxRequest(request));
    }

    /**
     * 사용자 친화적 오류 메시지 생성
     */
    private String getUserFriendlyMessage(Exception ex) {
        String message = ex.getMessage();

        // 일반적인 오류 패턴에 대한 사용자 친화적 메시지
        if (message != null) {
            if (message.contains("Failed to open device")) {
                return "Device connection failed. Please check if the device is connected and not in use by another application.";
            }
            if (message.contains("FPHPS_EPASS_DATA_IS_EMPTY")) {
                return "No passport data found. Please place the passport correctly on the reader.";
            }
            if (message.contains("timeout") || message.contains("Timeout")) {
                return "Operation timed out. Please try again.";
            }
        }

        return message != null ? message : "An unexpected error occurred.";
    }

    @ExceptionHandler(DeviceOperationException.class)
    public Object handleDeviceOperationException(DeviceOperationException ex, Model model,
                                                  HttpServletRequest request, HttpServletResponse response) {
        log.error("Device operation failed: {}", ex.getMessage(), ex);

        String userMessage = getUserFriendlyMessage(ex);

        if (isHtmxRequest(request)) {
            // HTMX 요청: Toast 메시지를 트리거하는 헤더와 함께 빈 응답 반환
            response.setHeader("HX-Trigger-Toast", URLEncoder.encode(userMessage, StandardCharsets.UTF_8));
            response.setHeader("HX-Trigger-Toast-Type", "error");
            response.setHeader("HX-Reswap", "none"); // DOM 업데이트 방지
            response.setStatus(HttpStatus.OK.value()); // 200 반환하여 HTMX가 정상 처리
            return ResponseEntity.ok().build();
        }

        // 일반 요청: 기존 에러 페이지로 리다이렉트
        model.addAttribute("errorMessage", userMessage);
        return "fragments/error_display";
    }

    @ExceptionHandler(DeviceNotFoundException.class)
    public Object handleDeviceNotFoundException(DeviceNotFoundException ex, Model model,
                                                HttpServletRequest request, HttpServletResponse response) {
        log.error("Device not found: {}", ex.getMessage(), ex);

        String userMessage = "No passport reader device found. Please check if the device is connected and try again.";

        if (isHtmxRequest(request)) {
            response.setHeader("HX-Trigger-Toast", URLEncoder.encode(userMessage, StandardCharsets.UTF_8));
            response.setHeader("HX-Trigger-Toast-Type", "error");
            response.setHeader("HX-Reswap", "none");
            response.setStatus(HttpStatus.OK.value());
            return ResponseEntity.ok().build();
        }

        model.addAttribute("errorMessage", userMessage);
        return "fragments/error_display";
    }

    @ExceptionHandler(DeviceNotOpenedException.class)
    public Object handleDeviceNotOpenedException(DeviceNotOpenedException ex, Model model,
                                                  HttpServletRequest request, HttpServletResponse response) {
        log.error("Device not opened: {}", ex.getMessage(), ex);

        String userMessage = "Device connection lost. Please reconnect the device and try again.";

        if (isHtmxRequest(request)) {
            response.setHeader("HX-Trigger-Toast", URLEncoder.encode(userMessage, StandardCharsets.UTF_8));
            response.setHeader("HX-Trigger-Toast-Type", "error");
            response.setHeader("HX-Reswap", "none");
            response.setStatus(HttpStatus.OK.value());
            return ResponseEntity.ok().build();
        }

        model.addAttribute("errorMessage", userMessage);
        return "fragments/error_display";
    }

    @ExceptionHandler(NativeLibraryException.class)
    public Object handleNativeLibraryException(NativeLibraryException ex, Model model,
                                               HttpServletRequest request, HttpServletResponse response) {
        log.error("Native library error: {}", ex.getMessage(), ex);

        String userMessage = "A system error occurred. Please restart the application and try again.";

        if (isHtmxRequest(request)) {
            response.setHeader("HX-Trigger-Toast", URLEncoder.encode(userMessage, StandardCharsets.UTF_8));
            response.setHeader("HX-Trigger-Toast-Type", "error");
            response.setHeader("HX-Reswap", "none");
            response.setStatus(HttpStatus.OK.value());
            return ResponseEntity.ok().build();
        }

        model.addAttribute("errorMessage", userMessage);
        return "fragments/error_display";
    }

    @ExceptionHandler(SODVerificationException.class)
    public Object handleSODVerificationException(SODVerificationException ex, Model model,
                                                  HttpServletRequest request, HttpServletResponse response) {
        log.error("SOD verification failed: {}", ex.getMessage(), ex);

        String userMessage = "Passport security verification failed. The document may be invalid or tampered.";

        if (isHtmxRequest(request)) {
            response.setHeader("HX-Trigger-Toast", URLEncoder.encode(userMessage, StandardCharsets.UTF_8));
            response.setHeader("HX-Trigger-Toast-Type", "warning");
            response.setHeader("HX-Reswap", "none");
            response.setStatus(HttpStatus.OK.value());
            return ResponseEntity.ok().build();
        }

        model.addAttribute("errorMessage", userMessage);
        return "fragments/error_display";
    }

    /**
     * PA 검증 예외 처리 - JSON 응답 반환
     */
    @ExceptionHandler(PassiveAuthenticationService.PaVerificationException.class)
    public ResponseEntity<Map<String, Object>> handlePaVerificationException(
            PassiveAuthenticationService.PaVerificationException ex,
            HttpServletRequest request) {
        log.error("PA verification failed: {}", ex.getMessage(), ex);

        String userMessage = ex.getMessage() != null ? ex.getMessage() : "PA verification failed.";

        // PA 검증은 항상 JSON API로 호출되므로 JSON 응답 반환
        Map<String, Object> errorResponse = Map.of(
            "success", false,
            "error", true,
            "message", userMessage,
            "status", "FAILURE"
        );

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .body(errorResponse);
    }

    @ExceptionHandler(FPHPSException.class)
    public Object handleFPHPSException(FPHPSException ex, Model model,
                                       HttpServletRequest request, HttpServletResponse response) {
        log.error("FPHPS error: {}", ex.getMessage(), ex);

        String userMessage = getUserFriendlyMessage(ex);

        if (isHtmxRequest(request)) {
            response.setHeader("HX-Trigger-Toast", URLEncoder.encode(userMessage, StandardCharsets.UTF_8));
            response.setHeader("HX-Trigger-Toast-Type", "error");
            response.setHeader("HX-Reswap", "none");
            response.setStatus(HttpStatus.OK.value());
            return ResponseEntity.ok().build();
        }

        model.addAttribute("errorMessage", userMessage);
        return "fragments/error_display";
    }

    @ExceptionHandler(Exception.class)
    public Object handleGenericException(Exception ex, Model model,
                                         HttpServletRequest request, HttpServletResponse response) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);

        String userMessage = getUserFriendlyMessage(ex);

        // JSON API 요청인 경우 JSON 응답 반환
        if (isJsonRequest(request)) {
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "error", true,
                "message", userMessage,
                "status", "FAILURE"
            );
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse);
        }

        if (isHtmxRequest(request)) {
            response.setHeader("HX-Trigger-Toast", URLEncoder.encode(userMessage, StandardCharsets.UTF_8));
            response.setHeader("HX-Trigger-Toast-Type", "error");
            response.setHeader("HX-Reswap", "none");
            response.setStatus(HttpStatus.OK.value());
            return ResponseEntity.ok().build();
        }

        model.addAttribute("errorMessage", userMessage);
        return "fragments/error_display";
    }
}
