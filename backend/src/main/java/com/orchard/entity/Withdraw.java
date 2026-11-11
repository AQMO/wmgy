package com.orchard.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 商家提现申请 */
@Data
@Entity
@Table(name = "t_withdraw")
public class Withdraw {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(precision = 14, scale = 2, nullable = false)
    private BigDecimal amount;

    /** PENDING 待审核 / APPROVED 已打款 / REJECTED 已驳回 */
    @Column(nullable = false, length = 10)
    private String status = "PENDING";

    @CreationTimestamp
    private LocalDateTime createdAt;
}
