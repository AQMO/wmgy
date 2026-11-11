package com.orchard.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/** 微信小程序登录请求 */
@Data
public class WxLoginReq {
    /** wx.login 获取的 code */
    @NotBlank(message = "code 不能为空")
    private String code;

    /** 微信昵称（选填） */
    private String nickname;
}
