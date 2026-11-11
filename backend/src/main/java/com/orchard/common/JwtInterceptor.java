package com.orchard.common;

import com.orchard.annotation.RoleRequired;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** JWT 认证 + 角色鉴权拦截器（白名单在 WebConfig 中配置） */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new BizException(BizException.UNAUTHORIZED, "未登录或登录已过期");
        }
        Claims claims;
        try {
            claims = jwtUtil.parse(auth.substring(7));
        } catch (JwtException | IllegalArgumentException e) {
            throw new BizException(BizException.UNAUTHORIZED, "登录已失效，请重新登录");
        }
        Long uid = Long.valueOf(claims.getSubject());
        String role = claims.get("role", String.class);
        request.setAttribute("uid", uid);
        request.setAttribute("role", role);

        // 角色校验：方法或类上的 @RoleRequired
        HandlerMethod hm = (HandlerMethod) handler;
        RoleRequired rr = hm.getMethodAnnotation(RoleRequired.class);
        if (rr == null) {
            rr = hm.getBeanType().getAnnotation(RoleRequired.class);
        }
        if (rr != null) {
            for (String r : rr.value()) {
                if (r.equals(role)) {
                    return true;
                }
            }
            throw new BizException(BizException.FORBIDDEN, "无权限访问");
        }
        return true;
    }
}
