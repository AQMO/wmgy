package com.orchard.config;

import com.orchard.common.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/** WebSocket 握手鉴权：URL 参数 token=JWT，校验通过后写入 uid/role 会话属性 */
@Component
public class WsHandshakeAuth implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    public WsHandshakeAuth(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest) {
            String token = ((ServletServerHttpRequest) request).getServletRequest().getParameter("token");
            if (token == null || token.isEmpty()) {
                return false;
            }
            try {
                Claims claims = jwtUtil.parse(token);
                attributes.put("uid", Long.valueOf(claims.getSubject()));
                attributes.put("role", claims.get("role", String.class));
                return true;
            } catch (JwtException | IllegalArgumentException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }
}
