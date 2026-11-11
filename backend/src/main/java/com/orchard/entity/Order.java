package com.orchard.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "t_order", indexes = {
        @Index(name = "idx_user_status", columnList = "user_id, status"),
        @Index(name = "idx_store_status", columnList = "store_id, status")
}, uniqueConstraints = @UniqueConstraint(name = "uk_order_no", columnNames = "order_no"))
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, length = 32)
    private String orderNo;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal freight = BigDecimal.ZERO;

    @Column(name = "pay_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal payAmount = BigDecimal.ZERO;

    /** 优惠券抵扣金额（营销二期） */
    @Column(name = "coupon_deduction", precision = 10, scale = 2)
    private BigDecimal couponDeduction = BigDecimal.ZERO;

    @Column(name = "user_coupon_id")
    private Long userCouponId;

    @Column(name = "commission_amount", precision = 10, scale = 2)
    private BigDecimal commissionAmount;   // 平台抽成（完成时）

    /** 抽成规则快照：规则修改仅对新订单生效 */
    @Column(name = "commission_mode", length = 10)
    private String commissionMode;   // RATIO / FIXED

    @Column(name = "commission_value", precision = 10, scale = 2)
    private BigDecimal commissionValue;

    /** UNPAID 待付款 / UNSHIPPED 待发货 / SHIPPED 已发货 / COMPLETED 已完成 / CANCELLED 已取消 / REFUNDED 已退款 */
    @Column(nullable = false, length = 20)
    private String status = "UNPAID";

    @Column(name = "express_company", length = 50)
    private String expressCompany;

    @Column(name = "express_no", length = 50)
    private String expressNo;

    @Column(name = "receiver_name", length = 50)
    private String receiverName;

    @Column(name = "receiver_phone", length = 20)
    private String receiverPhone;

    @Column(name = "receiver_address", length = 255)
    private String receiverAddress;

    @Column(length = 300)
    private String remark;

    private LocalDateTime paidAt;
    private LocalDateTime shippedAt;
    private LocalDateTime completedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
}
