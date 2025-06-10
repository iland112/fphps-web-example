package com.smartcoreinc.fphps.example.fphps_web_example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.smartcoreinc.fphps.example.fphps_web_example.config.handler.FastPassWebSocketHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final FastPassWebSocketHandler fastPassWebSocketHandler;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.debug("[+] 최초 WebSocket 연결을 위한 등록 Handler");
        registry
            // 클라이언트에서 웹 소켓 연결을 위해 "fastpass"라는 엔드포인트로 연결을 시도하면 FastPassWebSocketHandler 클래스에서 이를 처리합니다.
            .addHandler(fastPassWebSocketHandler, "fastpass")
            // 접속 시도하는 모든 도메인 또는 IP에서 WebSocket 연결을 허용합니다.
            .setAllowedOrigins("*");    
    }

}
