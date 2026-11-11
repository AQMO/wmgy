package com.orchard.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/** 聊天消息：买家(用户) ↔ 果园商家 一对一会话 */
@Data
@Entity
@Table(name = "t_chat_message", indexes = {
        @Index(name = "idx_conv", columnList = "conversationKey"),
        @Index(name = "idx_store", columnList = "storeId, createdAt")
})
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 会话键：u{userId}_s{storeId} */
    @Column(name = "conversationKey", nullable = false, length = 60)
    private String conversationKey;

    @Column(name = "userId", nullable = false)
    private Long userId;

    @Column(name = "storeId", nullable = false)
    private Long storeId;

    /** USER 买家发送 / MERCHANT 商家发送 */
    @Column(name = "senderRole", nullable = false, length = 10)
    private String senderRole;

    @Column(nullable = false, length = 500)
    private String content;

    /** 接收方是否已读 */
    private Boolean readFlag = false;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
