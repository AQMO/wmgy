package com.orchard.service.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orchard.entity.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

/**
 * 聊天 WebSocket：连接即注册；客户端可直发 {"type":"chat","storeId":1,"content":"hi"}
 * （买家）或 {"type":"chat","peerUserId":2,"content":"hi"}（商家）；发送后由 ChatService 双端推送。
 * 断线期间消息不丢：REST/HTTP 兜底 + 历史拉取。
 */
@Slf4j
@Component
public class ChatWsHandler extends TextWebSocketHandler {

    private final WsSessionRegistry registry;
    private final ChatGateway gateway;
    private final ObjectMapper objectMapper;

    public ChatWsHandler(WsSessionRegistry registry, ChatGateway gateway, ObjectMapper objectMapper) {
        this.registry = registry;
        this.gateway = gateway;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long uid = (Long) session.getAttributes().get("uid");
        if (uid != null) {
            registry.register(uid, session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        Long uid = (Long) session.getAttributes().get("uid");
        String role = (String) session.getAttributes().get("role");
        if (uid == null) {
            return;
        }
        try {
            JsonNode node = objectMapper.readTree(message.getPayload());
            String type = node.path("type").asText("");
            if ("ping".equals(type)) {
                registry.sendToUser(uid, "{\"type\":\"pong\"}");
                return;
            }
            if ("chat".equals(type)) {
                Long storeId = node.hasNonNull("storeId") ? node.get("storeId").asLong() : null;
                Long peerUserId = node.hasNonNull("peerUserId") ? node.get("peerUserId").asLong() : null;
                String content = node.path("content").asText("");
                ChatMessage msg = gateway.sendFromWs(uid, role, storeId, peerUserId, content);
                if (msg == null) {
                    registry.sendToUser(uid, "{\"type\":\"error\",\"data\":{\"message\":\"发送失败\"}}");
                }
            }
        } catch (Exception e) {
            log.warn("WS 消息处理失败: uid={}", uid, e);
            try {
                registry.sendToUser(uid, "{\"type\":\"error\",\"data\":{\"message\":\""
                        + e.getMessage() + "\"}}");
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long uid = (Long) session.getAttributes().get("uid");
        if (uid != null) {
            registry.remove(uid, session);
        }
    }
}
