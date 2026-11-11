package com.orchard.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/** 创建订单请求（可含多店铺商品，系统按店铺拆单） */
@Data
public class OrderCreateReq {

    @Valid
    @NotEmpty(message = "请选择商品")
    private List<Item> items;

    @NotBlank(message = "收货人不能为空")
    private String receiverName;

    @NotBlank(message = "收货电话不能为空")
    private String receiverPhone;

    @NotBlank(message = "收货地址不能为空")
    private String receiverAddress;

    private String remark;

    @Data
    public static class Item {
        private Long productId;

        private Integer quantity = 1;
    }
}
