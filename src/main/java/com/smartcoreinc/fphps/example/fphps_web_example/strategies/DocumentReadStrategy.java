package com.smartcoreinc.fphps.example.fphps_web_example.strategies;

import com.smartcoreinc.fphps.dto.DocumentReadResponse;
import com.smartcoreinc.fphps.manager.FPHPSDevice;
import com.smartcoreinc.fphps.example.fphps_web_example.config.handler.FastPassWebSocketHandler;

public interface DocumentReadStrategy {
    boolean supports(String docType);
    DocumentReadResponse read(FPHPSDevice device, FastPassWebSocketHandler fastPassWebSocketHandler, boolean isAuto);
}