package com.orchard.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/** 商家入驻申请（PRD 4.1） */
@Data
public class StoreApplyReq {
    @NotBlank(message = "果园名称不能为空")
    private String name;

    @NotBlank(message = "果园地址不能为空")
    private String address;

    private String intro;
    private String logo;
    private String cover;
}
