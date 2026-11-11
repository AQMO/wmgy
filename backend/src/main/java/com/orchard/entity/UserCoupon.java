package com.orchard.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/** 用户持券记录 */
@Data
@Entity
@Table(name = "t_user_coupon", indexes = @Index(name = "idx_uc_user", columnList = "user_id, status"))
public class UserCoupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coupon_id", nullable = false)
    private Long couponId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** UNUSED 可用 / USED 已使用 / EXPIRED 已过期 */
    @Column(nullable = false, length = 10)
    private String status = "UNUSED";

    @Column(name = "order_id")
    private Long orderId;

    private LocalDateTime usedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
