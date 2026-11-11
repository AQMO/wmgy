package com.orchard.controller;

import com.orchard.common.Result;
import com.orchard.dto.LoginReq;
import com.orchard.dto.LoginResp;
import com.orchard.dto.RegisterReq;
import com.orchard.dto.WxLoginReq;
import com.orchard.entity.User;
import com.orchard.service.AuthService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** 消费者注册 */
    @PostMapping("/register")
    public Result<LoginResp> register(@Validated @RequestBody RegisterReq req) {
        return Result.ok(authService.register(req));
    }

    /** 登录（三端统一入口，按角色跳转） */
    @PostMapping("/login")
    public Result<LoginResp> login(@Validated @RequestBody LoginReq req) {
        return Result.ok(authService.login(req));
    }

    /** 当前用户信息 */
    @GetMapping("/me")
    public Result<User> me(@RequestAttribute("uid") Long uid) {
        return Result.ok(authService.me(uid));
    }

    /** 微信小程序登录（wx.login code 换取 Token） */
    @PostMapping("/wxlogin")
    public Result<LoginResp> wxLogin(@Validated @RequestBody WxLoginReq req) {
        return Result.ok(authService.wxLogin(req));
    }
}
