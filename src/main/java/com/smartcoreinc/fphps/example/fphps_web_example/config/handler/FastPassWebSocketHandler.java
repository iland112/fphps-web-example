package com.smartcoreinc.fphps.example.fphps_web_example.config.handler;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.cert.X509Certificate;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.smartcoreinc.fphps.dto.DocumentReadResponse;
import com.smartcoreinc.fphps.dto.EventMessageData;
import com.smartcoreinc.fphps.sod.ParsedSOD;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.ParsedSODInfo;
import com.smartcoreinc.fphps.interfaces.MessageBroadcastable;
import com.smartcoreinc.fphps.readers.EPassportReader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FastPassWebSocketHandler extends TextWebSocketHandler implements MessageBroadcastable {

    private static final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    // Auto Read 완료 시 DocumentReadResponse를 저장할 콜백
    private volatile Consumer<DocumentReadResponse> onReadCompleteCallback;

    // 현재 사용 중인 EPassportReader 인스턴스 (Auto Read 시 결과 조회용)
    private volatile EPassportReader currentReader;

    /**
     * Auto Read 완료 시 호출될 콜백 설정
     * @param callback DocumentReadResponse를 받아 처리할 Consumer
     */
    public void setOnReadCompleteCallback(Consumer<DocumentReadResponse> callback) {
        this.onReadCompleteCallback = callback;
    }

    /**
     * 현재 사용 중인 Reader 설정 (Auto Read 결과 조회용)
     * @param reader EPassportReader 인스턴스
     */
    public void setCurrentReader(EPassportReader reader) {
        this.currentReader = reader;
    }

    /**
     * Reader 정리 (Auto Read 완료 또는 취소 시)
     */
    public void clearCurrentReader() {
        this.currentReader = null;
    }

    // Gson with custom serializer for ParsedSOD to convert X509Certificate to DTO
    private static final Gson gson = new GsonBuilder()
        .registerTypeAdapter(ParsedSOD.class, new JsonSerializer<ParsedSOD>() {
            @Override
            public JsonElement serialize(ParsedSOD src, Type typeOfSrc, JsonSerializationContext context) {
                if (src == null) {
                    return null;
                }
                // ParsedSOD를 ParsedSODInfo DTO로 변환하여 직렬화
                ParsedSODInfo sodInfo = ParsedSODInfo.from(src);
                return context.serialize(sodInfo);
            }
        })
        .create();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.debug("[+] a new client connected with session id [{}]", session.getId());
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.debug("[+] a client disconnected with session id [{}], close status: [{}]", session.getId(), status.toString());
        sessions.remove(session);
    }

    @Override
    public void broadcast(EventMessageData message) {
        // Auto Read 완료 이벤트 감지 및 결과 저장
        if ("FPHPS_EVENTS.FPHPS_EV_EPASS_READ_DONE".equals(message.getEventCodeString())) {
            handleAutoReadComplete();
        }

        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    // ParsedSOD 존재 여부 로깅
                    if (message.getParsedSOD() != null) {
                        log.debug("Broadcasting message with ParsedSOD data");
                    }

                    String jsonMessage = gson.toJson(message);
                    session.sendMessage(new TextMessage(jsonMessage));
                    log.debug("Sent message to session :: {}, event message code string :: {}", session.getId(), message.getEventCodeString());
                }
            } catch (Exception e) {
                log.error("Failed to broadcast message to session {}: {}", session.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Auto Read 완료 시 호출되어 결과를 콜백으로 전달
     */
    private void handleAutoReadComplete() {
        if (currentReader != null && onReadCompleteCallback != null) {
            try {
                DocumentReadResponse response = currentReader.getDocumentData();
                if (response != null) {
                    log.info("Auto Read completed - Retrieved DocumentReadResponse: passportNumber={}, SOD size={}",
                        response.getMrzInfo() != null ? response.getMrzInfo().getPassportNumber() : "null",
                        response.getSodDataBytes() != null ? response.getSodDataBytes().length : 0);
                    onReadCompleteCallback.accept(response);
                } else {
                    log.warn("Auto Read completed but getDocumentData() returned null");
                }
            } catch (Exception e) {
                log.error("Failed to retrieve Auto Read result: {}", e.getMessage(), e);
            } finally {
                // Reader 정리
                clearCurrentReader();
            }
        } else {
            log.debug("Auto Read completed but no reader or callback registered (currentReader={}, callback={})",
                currentReader != null ? "set" : "null",
                onReadCompleteCallback != null ? "set" : "null");
        }
    }
}
