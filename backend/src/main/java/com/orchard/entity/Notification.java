package com.orchard.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/** 站内消息通知：下单/发货/收货/审核/资金等事件推送 */
@Data
@Entity
@Table(name = "t_notification", indexes = @Index(name = "idx_user_read", columnList = "userId, readFlag"))
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "userId", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(length = 300)
    private String content;

    /** ORDER / SHIP / AUDIT / SETTLE 等 */
    @Column(name = "bizType", length = 20)
    private String bizType;

    @Column(name = "bizId")
    private Long bizId;

    private Boolean readFlag = false;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
