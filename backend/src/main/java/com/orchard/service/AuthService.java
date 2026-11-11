package com.orchard.service;

import com.orchard.common.BizException;
import com.orchard.common.JwtUtil;
import com.orchard.dto.LoginReq;
import com.orchard.dto.LoginResp;
import com.orchard.dto.RegisterReq;
import com.orchard.dto.WxLoginReq;
import com.orchard.entity.User;
import com.orchard.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Value("${wx.appid:}")
    private String wxAppid;

    @Value("${wx.secret:}")
    private String wxSecret;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    /** 消费者注册 */
    public LoginResp register(RegisterReq req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new BizException("用户名已存在");
        }
        User u = new User();
        u.setUsername(req.getUsername());
        u.setPassword(encoder.encode(req.getPassword()));
        u.setNickname(req.getNickname() != null ? req.getNickname() : req.getUsername());
        u.setPhone(req.getPhone());
        u.setRole("USER");
        u = userRepository.save(u);
        return new LoginResp(jwtUtil.generate(u.getId(), u.getRole()), u.getId(), u.getUsername(), u.getNickname(), u.getRole());
    }

    public LoginResp login(LoginReq req) {
        User u = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new BizException("用户名或密码错误"));
        if (!encoder.matches(req.getPassword(), u.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        if (u.getStatus() == 0) {
            throw new BizException("账号已被禁用");
        }
        return new LoginResp(jwtUtil.generate(u.getId(), u.getRole()), u.getId(), u.getUsername(), u.getNickname(), u.getRole());
    }

    public User me(Long uid) {
        return userRepository.findById(uid).orElseThrow(() -> new BizException("用户不存在"));
    }

    /** 微信小程序登录：配置了 appid/secret 走真实 jscode2session，否则用开发模式（mock openid） */
    public LoginResp wxLogin(WxLoginReq req) {
        String openid = resolveOpenid(req.getCode());
        String username = "wx_" + openid;
        User u = userRepository.findByUsername(username).orElse(null);
        if (u == null) {
            u = new User();
            u.setUsername(username);
            u.setPassword(encoder.encode("wx-" + openid));   // 随机占位密码，不走账密登录
            u.setNickname(req.getNickname() != null && !req.getNickname().isEmpty()
                    ? req.getNickname() : "微信用户" + openid.substring(Math.max(0, openid.length() - 4)));
            u.setRole("USER");
            u = userRepository.save(u);
            log.info("新微信用户注册: {}", username);
        }
        if (u.getStatus() == 0) throw new BizException("账号已被禁用");
        return new LoginResp(jwtUtil.generate(u.getId(), u.getRole()), u.getId(), u.getUsername(), u.getNickname(), u.getRole());
    }

    private String resolveOpenid(String code) {
        if (wxAppid == null || wxAppid.isEmpty()) {
            // 开发模式：无 AppID 时按 code 生成固定 mock openid，便于本地调试
            return "mock_" + code;
        }
        try {
            String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + wxAppid
                    + "&secret=" + wxSecret + "&js_code=" + code + "&grant_type=authorization_code";
            String resp = new RestTemplate().getForObject(url, String.class);
            JsonNode node = new ObjectMapper().readTree(resp);
            String openid = node.path("openid").asText(null);
            if (openid == null) {
                throw new BizException("微信登录失败：" + node.path("errmsg").asText("未知错误"));
            }
            return openid;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("jscode2session 调用失败", e);
            throw new BizException("微信登录服务异常，请稍后重试");
        }
    }
}
