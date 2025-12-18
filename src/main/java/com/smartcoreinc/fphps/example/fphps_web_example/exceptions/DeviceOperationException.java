package com.smartcoreinc.fphps.example.fphps_web_example.exceptions;

public class DeviceOperationException extends RuntimeException {
    public DeviceOperationException(String message) {
        super(message);
    }

    public DeviceOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}