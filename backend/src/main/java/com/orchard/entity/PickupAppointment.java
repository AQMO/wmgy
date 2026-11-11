package com.orchard.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 采摘/到园自提预约（二期：PRD 采摘预约场景） */
@Data
@Entity
@Table(name = "t_pickup_appointment", indexes = @Index(name = "idx_pa_store_status", columnList = "store_id, status"))
public class PickupAppointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name", length = 100)
    private String productName;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "pick_date", nullable = false)
    private LocalDate pickDate;

    /** 上午/下午/傍晚 */
    @Column(name = "time_slot", nullable = false, length = 10)
    private String timeSlot;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(length = 20)
    private String phone;

    /** PENDING 待确认 / CONFIRMED 已确认 / REJECTED 已拒绝 / CANCELLED 已取消 / DONE 已完成 */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(length = 200)
    private String remark;

    @Column(name = "store_reply", length = 200)
    private String storeReply;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
