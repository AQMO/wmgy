package com.orchard.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/** 自助发货/修改物流请求（PRD 4.5 发货弹窗） */
@Data
public class ShipReq {
    @NotBlank(message = "快递公司不能为空")
    private String expressCompany;

    @NotBlank(message = "快递单号不能为空")
    @Size(max = 50, message = "快递单号过长")
    private String expressNo;

    /** 选填：发货备注（易碎、加急等） */
    private String remark;
}
