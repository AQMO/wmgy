package com.orchard.service.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/** 服务端主动推送：聊天消息、站内通知（在线即推，离线由轮询/拉取兜底） */
@Slf4j
@Service
public class WsPushService {

    private final WsSessionRegistry registry;
    private final ObjectMapper objectMapper;

    public WsPushService(WsSessionRegistry registry, ObjectMapper objectMapper) {
        this.registry = registry;
        this.objectMapper = objectMapper;
    }

    public boolean push(Long uid, String type, Object data) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("type", type);
            body.put("data", data);
            String payload = objectMapper.writeValueAsString(body);
            return registry.sendToUser(uid, payload);
        } catch (Exception e) {
            log.warn("WS 推送失败: uid={} type={}", uid, type, e);
            return false;
        }
    }

    /** 推送聊天消息：data 为 ChatMessage */
    public boolean pushChat(Long uid, Object chatMessage) {
        return push(uid, "chat", chatMessage);
    }

    /** 推送站内通知：data 为 Notification */
    public boolean pushNotify(Long uid, Object notification) {
        return push(uid, "notify", notification);
    }
}
