package com.smartcoreinc.fphps.example.fphps_web_example.config.handler;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.GsonBuilder;
import com.smartcoreinc.fphps.dto.EventMessageData;
import com.smartcoreinc.fphps.interfaces.MessageBroadcastable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FastPassWebSocketHandler extends TextWebSocketHandler implements MessageBroadcastable {
    
    private static final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

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
                    String jsonMessage = new GsonBuilder().create().toJson(message);
                    session.sendMessage(new TextMessage(jsonMessage));
                    log.debug("Sent message to session :: {}, event message code string :: {}", session.getId(), message.getEventCodeString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
