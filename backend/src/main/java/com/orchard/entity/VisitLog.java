package com.orchard.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/** 店铺访问流量日志（小店流量监控数据源） */
@Data
@Entity
@Table(name = "t_visit_log", indexes = @Index(name = "idx_store_time", columnList = "storeId, createdAt"))
public class VisitLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "storeId", nullable = false)
    private Long storeId;

    /** 未登录访客为 null */
    @Column(name = "userId")
    private Long userId;

    /** 访问页面：home / store / product 等 */
    @Column(nullable = false, length = 30)
    private String page;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
