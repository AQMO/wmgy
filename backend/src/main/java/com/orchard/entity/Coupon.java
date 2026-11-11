package com.orchard.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 店铺优惠券（二期营销）：满 threshold 减 amount */
@Data
@Entity
@Table(name = "t_coupon", indexes = @Index(name = "idx_coupon_store", columnList = "store_id"))
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(nullable = false, length = 50)
    private String name;

    /** 使用门槛（满 X 元可用），0 表示无门槛 */
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal threshold = BigDecimal.ZERO;

    /** 优惠金额（减 Y 元） */
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    private Integer total = 0;

    private Integer remain = 0;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** 1 生效 / 0 停用 */
    private Integer status = 1;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
