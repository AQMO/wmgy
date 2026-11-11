package com.orchard.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_product", indexes = @Index(name = "idx_store_status", columnList = "store_id, status"))
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    private Long categoryId;

    @Column(nullable = false, length = 100)
    private String name;

    private String cover;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock = 0;

    private Integer sales = 0;

    @Column(columnDefinition = "TEXT")
    private String detail;   // 详情富文本

    /** ON_SHELF 上架 / OFF_SHELF 下架 / DRAFT 草稿 */
    @Column(nullable = false, length = 10)
    private String status = "DRAFT";

    @CreationTimestamp
    private LocalDateTime createdAt;
}
