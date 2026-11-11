package com.orchard.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class ProductReq {
    @NotBlank(message = "商品名称不能为空")
    private String name;

    private Long categoryId;

    private String cover;

    @NotNull(message = "售价不能为空")
    @DecimalMin(value = "0.01", message = "售价必须大于 0")
    private BigDecimal price;

    @NotNull(message = "库存不能为空")
    private Integer stock;

    private String detail;

    /** ON_SHELF / OFF_SHELF / DRAFT */
    private String status = "DRAFT";
}
