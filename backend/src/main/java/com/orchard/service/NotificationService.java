package com.orchard.service;

import com.orchard.entity.Notification;
import com.orchard.repository.NotificationRepository;
import com.orchard.service.ws.WsPushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 站内消息通知：事件落库 + WebSocket 实时推送（离线由拉取兜底） */
@Slf4j
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final WsPushService wsPushService;

    public NotificationService(NotificationRepository notificationRepository, WsPushService wsPushService) {
        this.notificationRepository = notificationRepository;
        this.wsPushService = wsPushService;
    }

    @Transactional
    public void push(Long userId, String title, String content, String bizType, Long bizId) {
        if (userId == null) return;
        try {
            Notification n = new Notification();
            n.setUserId(userId);
            n.setTitle(title);
            n.setContent(content);
            n.setBizType(bizType);
            n.setBizId(bizId);
            notificationRepository.save(n);
            // 实时推送（用户不在线则静默，由消息中心拉取）
            wsPushService.pushNotify(userId, n);
        } catch (Exception e) {
            // 通知失败不阻断主业务
            log.warn("消息推送失败: userId={} title={}", userId, title, e);
        }
    }

    public Page<Notification> list(Long userId, int page, int size) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
    }

    public long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFlagFalse(userId);
    }

    @Transactional
    public void markRead(Long userId, Long id) {
        notificationRepository.markRead(id, userId);
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllRead(userId);
    }
}
