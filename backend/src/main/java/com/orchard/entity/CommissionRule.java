package com.orchard.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 平台抽成规则（版本化：仅 effective=1 为当前生效，修改生成新版本，旧订单快照不受影响） */
@Data
@Entity
@Table(name = "t_commission_rule")
public class CommissionRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** RATIO 比例抽成 / FIXED 固定金额抽成 */
    @Column(nullable = false, length = 10)
    private String mode;

    /** 比例 %（mode=RATIO） */
    @Column(precision = 5, scale = 2)
    private BigDecimal ratio;

    /** 固定金额（mode=FIXED） */
    @Column(name = "fixed_amount", precision = 10, scale = 2)
    private BigDecimal fixedAmount;

    /** 业务类型：NORMAL 常规商品（二期预留 PICKING 采摘 / TICKET 面单等独立抽成） */
    @Column(name = "biz_type", nullable = false, length = 20)
    private String bizType = "NORMAL";

    /** 运费是否计入抽成基数 */
    @Column(name = "include_freight")
    private Boolean includeFreight = false;

    /** 当前生效版本标记 */
    @Column(nullable = false)
    private Integer effective = 1;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
