package com.orchard.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

@Data
public class WithdrawReq {
    @DecimalMin(value = "0.01", message = "提现金额必须大于 0")
    private BigDecimal amount;
}
