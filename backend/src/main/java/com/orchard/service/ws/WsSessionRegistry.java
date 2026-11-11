package com.orchard.service.ws;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/** 在线会话注册表：userId → 该用户所有活跃 WebSocket 连接（支持多端同时在线） */
@Component
public class WsSessionRegistry {

    private final Map<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    public void register(Long uid, WebSocketSession session) {
        userSessions.computeIfAbsent(uid, k -> new CopyOnWriteArraySet<>()).add(session);
    }

    public void remove(Long uid, WebSocketSession session) {
        Set<WebSocketSession> set = userSessions.get(uid);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) {
                userSessions.remove(uid, set);
            }
        }
    }

    /** 向用户全部在线连接推送文本，任一成功即视为送达 */
    public boolean sendToUser(Long uid, String payload) {
        Set<WebSocketSession> set = userSessions.get(uid);
        if (set == null || set.isEmpty()) {
            return false;
        }
        boolean any = false;
        for (WebSocketSession s : set) {
            if (s.isOpen()) {
                try {
                    synchronized (s) {
                        s.sendMessage(new TextMessage(payload));
                    }
                    any = true;
                } catch (IOException | IllegalStateException ignored) {
                    // 连接异常由 handleClose 回收
                }
            }
        }
        return any;
    }

    public boolean isOnline(Long uid) {
        Set<WebSocketSession> set = userSessions.get(uid);
        return set != null && set.stream().anyMatch(WebSocketSession::isOpen);
    }
}
