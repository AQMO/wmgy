package com.orchard.service.ws;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/** 通知 WebSocket：仅维护在线连接，服务端事件发生时经 WsPushService 主动推送 */
@Component
public class NotifyWsHandler extends TextWebSocketHandler {

    private final WsSessionRegistry registry;

    public NotifyWsHandler(WsSessionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long uid = (Long) session.getAttributes().get("uid");
        if (uid != null) {
            registry.register(uid, session);
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
