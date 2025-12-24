package com.smartcoreinc.fphps.example.fphps_web_example.advice;

import com.smartcoreinc.fphps.example.fphps_web_example.exceptions.DeviceOperationException;
import com.smartcoreinc.fphps.exception.FPHPSException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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

        String userMessage = "An unexpected error occurred. Please try again.";

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
