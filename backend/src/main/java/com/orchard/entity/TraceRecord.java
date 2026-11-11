package com.orchard.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/** 商品溯源记录（二期）：种植/生长/采摘/质检/包装全链路 */
@Data
@Entity
@Table(name = "t_trace_record", indexes = @Index(name = "idx_trace_product", columnList = "product_id"))
public class TraceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    /** 种植 / 施肥 / 疏果 / 采摘 / 质检 / 包装 / 入库 */
    @Column(nullable = false, length = 20)
    private String stage;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    /** 图片凭证（文件记录 URL） */
    @Column(columnDefinition = "TEXT")
    private String images;

    @Column(name = "operator_id")
    private Long operatorId;

    /** 溯源发生的现场时间（可为空，默认 createdAt） */
    private LocalDateTime eventTime;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
