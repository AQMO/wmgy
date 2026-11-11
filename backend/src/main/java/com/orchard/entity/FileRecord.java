package com.orchard.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/** 文件记录表：所有上传文件（MinIO/本地）统一登记，可维护、可追溯、可删除 */
@Data
@Entity
@Table(name = "t_file", indexes = @Index(name = "idx_file_biz", columnList = "biz"))
public class FileRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 外部访问 URL（MinIO 直链或本地接口地址） */
    @Column(nullable = false, length = 500)
    private String url;

    /** 存储端对象 Key（MinIO objectName / 本地文件名），删除与变更依据 */
    @Column(name = "object_key", nullable = false, length = 300)
    private String objectKey;

    /** MINIO / LOCAL */
    @Column(nullable = false, length = 10)
    private String storage;

    @Column(name = "file_name", length = 200)
    private String fileName;

    @Column(name = "content_type", length = 100)
    private String contentType;

    private Long size;

    /** 业务归属：PRODUCT / CHAT / TRACE / AVATAR 等，便于按模块维护 */
    @Column(length = 30)
    private String biz;

    @Column(name = "uploader_id")
    private Long uploaderId;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
