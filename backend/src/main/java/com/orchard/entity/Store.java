package com.orchard.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 果园店铺：一果园一店，多商户数据隔离核心 */
@Data
@Entity
@Table(name = "t_store")
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", unique = true, nullable = false)
    private Long ownerId;

    @Column(nullable = false, length = 100)
    private String name;

    private String logo;
    private String cover;
    @Column(length = 500)
    private String intro;
    @Column(length = 300)
    private String notice;
    private String address;

    /** PENDING 待审核 / ACTIVE 营业 / FROZEN 冻结 / CLOSED 关停 */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    /** 保证金 */
    private BigDecimal deposit = BigDecimal.ZERO;

    /** 专属抽成比例（%），null 时使用全局/品类规则 */
    private BigDecimal commissionRate;

    /** 线下自提开关（PRD 2.3-5） */
    private Boolean selfPickup = false;

    /** 可提现余额 */
    private BigDecimal balance = BigDecimal.ZERO;

    /** 冻结金额（已完成前订单货款） */
    private BigDecimal frozenAmount = BigDecimal.ZERO;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
