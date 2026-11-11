package com.orchard.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orchard.common.JwtUtil;
import com.orchard.service.ws.ChatWsHandler;
import com.orchard.service.ws.NotifyWsHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/** WebSocket 注册：/ws/chat 聊天、/ws/notify 站内通知；握手阶段 ?token= 鉴权 */
@Configuration
@EnableWebSocket
public class WsConfig implements WebSocketConfigurer {

    private final ChatWsHandler chatWsHandler;
    private final NotifyWsHandler notifyWsHandler;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public WsConfig(ChatWsHandler chatWsHandler, NotifyWsHandler notifyWsHandler,
                    JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.chatWsHandler = chatWsHandler;
        this.notifyWsHandler = notifyWsHandler;
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        WsHandshakeAuth auth = new WsHandshakeAuth(jwtUtil);
        registry.addHandler(chatWsHandler, "/ws/chat").addInterceptors(auth).setAllowedOriginPatterns("*");
        registry.addHandler(notifyWsHandler, "/ws/notify").addInterceptors(auth).setAllowedOriginPatterns("*");
    }
}
