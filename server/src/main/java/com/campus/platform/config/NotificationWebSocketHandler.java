package com.campus.platform.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.campus.platform.security.JwtTokenService;
import io.jsonwebtoken.Claims;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final JwtTokenService jwtTokenService;
    private final ObjectMapper objectMapper;
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = null;
        if (session.getUri() != null && session.getUri().getQuery() != null) {
            for (String query : session.getUri().getQuery().split("&")) {
                if (query.startsWith("token=")) {
                    token = query.substring(6);
                    break;
                }
            }
        }
        if (token == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }
        Claims claims = jwtTokenService.parse(token);
        Long userId = Long.valueOf(claims.getSubject());
        sessions.put(userId, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.entrySet().removeIf(entry -> entry.getValue().getId().equals(session.getId()));
    }

    public void sendToUser(Long userId, Object payload) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
            } catch (IOException ignored) {
            }
        }
    }
}
