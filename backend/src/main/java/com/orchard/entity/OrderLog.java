package com.orchard.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/** 订单日志：状态变更、物流修改留痕（PRD 2.3-4），永久留存 */
@Data
@Entity
@Table(name = "t_order_log")
public class OrderLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false, length = 30)
    private String action;

    @Column(length = 500)
    private String detail;

    private Long operator;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
