package com.smartcoreinc.fphps.example.fphps_web_example.config.handler;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.cert.X509Certificate;
import java.util.concurrent.CopyOnWriteArrayList;

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
import com.smartcoreinc.fphps.dto.EventMessageData;
import com.smartcoreinc.fphps.sod.ParsedSOD;
import com.smartcoreinc.fphps.example.fphps_web_example.dto.ParsedSODInfo;
import com.smartcoreinc.fphps.interfaces.MessageBroadcastable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FastPassWebSocketHandler extends TextWebSocketHandler implements MessageBroadcastable {

    private static final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

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
}
