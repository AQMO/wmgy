package com.orchard.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/** 全局操作日志：永久留存，不可删除（PRD 非功能 6） */
@Data
@Entity
@Table(name = "t_operation_log")
public class OperationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(nullable = false, length = 30)
    private String module;

    @Column(nullable = false, length = 30)
    private String action;

    @Column(length = 500)
    private String detail;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
