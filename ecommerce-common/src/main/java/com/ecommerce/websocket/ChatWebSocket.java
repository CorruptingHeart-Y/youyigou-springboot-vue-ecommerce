package com.ecommerce.websocket;

import cn.hutool.json.JSONUtil;
import com.ecommerce.security.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ConditionalOnExpression("'${spring.application.name}' != 'api-gateway'")
@ServerEndpoint("/ws/chat/{token}")
public class ChatWebSocket {

    private static final Map<Long, Session> onlineUsers = new ConcurrentHashMap<>();
    private static ApplicationContext applicationContext;

    private Long userId;
    private String username;

    public static void setApplicationContext(ApplicationContext ctx) {
        ChatWebSocket.applicationContext = ctx;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        try {
            JwtUtil jwtUtil = applicationContext.getBean(JwtUtil.class);
            Claims claims = jwtUtil.parseToken(token);
            this.userId = claims.get("userId", Long.class);
            this.username = claims.getSubject();
            onlineUsers.put(userId, session);
            broadcastSystemMessage(username + " joined chat");
            log.info("WebSocket connected: userId={}", userId);
        } catch (Exception e) {
            log.error("WebSocket auth failed", e);
            try { session.close(); } catch (IOException ignored) {}
        }
    }

    @OnMessage
    public void onMessage(String message) {
        if (userId == null) return;
        try {
            Map<String, Object> msgData = JSONUtil.parseObj(message);
            String content = (String) msgData.get("content");
            Long targetUserId = msgData.get("targetUserId") != null ? Long.valueOf(msgData.get("targetUserId").toString()) : null;

            Map<String, Object> response = new java.util.HashMap<>();
            response.put("fromUserId", userId);
            response.put("fromUsername", username);
            response.put("content", content);
            response.put("time", System.currentTimeMillis());
            String json = JSONUtil.toJsonStr(response);

            if (targetUserId != null) {
                sendToUser(targetUserId, json);
                sendToUser(userId, json);
            } else {
                broadcast(json);
            }
        } catch (Exception e) {
            log.error("Message handling failed", e);
        }
    }

    @OnClose
    public void onClose() {
        if (userId != null) {
            onlineUsers.remove(userId);
            broadcastSystemMessage(username + " left chat");
        }
    }

    @OnError
    public void onError(Throwable error) {
        log.error("WebSocket error", error);
    }

    private void sendToUser(Long userId, String message) {
        Session session = onlineUsers.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException ignored) {}
        }
    }

    private void broadcast(String message) {
        onlineUsers.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException ignored) {}
            }
        });
    }

    private void broadcastSystemMessage(String content) {
        Map<String, Object> msg = new java.util.HashMap<>();
        msg.put("type", "system");
        msg.put("content", content);
        msg.put("time", System.currentTimeMillis());
        broadcast(JSONUtil.toJsonStr(msg));
    }
}