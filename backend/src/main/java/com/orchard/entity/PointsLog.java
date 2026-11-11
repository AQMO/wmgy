package com.orchard.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/** 会员积分流水（二期）：确认收货得积分 = 实付金额整数部分 */
@Data
@Entity
@Table(name = "t_points_log", indexes = @Index(name = "idx_points_user", columnList = "user_id"))
public class PointsLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "order_id")
    private Long orderId;

    private Integer delta;

    private Integer balance;

    /** ORDER_EARN / SIGN / ADJUST */
    @Column(nullable = false, length = 20)
    private String type;

    @Column(length = 200)
    private String remark;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
