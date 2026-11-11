package com.orchard.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 商家资金流水 */
@Data
@Entity
@Table(name = "t_settlement_flow", indexes = @Index(name = "idx_store_type", columnList = "store_id, type"))
public class SettlementFlow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    private Long orderId;

    /** DEPOSIT 缴保证金 / ORDER_IN 货款入账 / COMMISSION 平台抽成 / WITHDRAW 提现 / REFUND 退款 */
    @Column(nullable = false, length = 20)
    private String type;

    @Column(precision = 14, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "balance_after", precision = 14, scale = 2)
    private BigDecimal balanceAfter;

    @Column(length = 300)
    private String remark;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
