package com.orchard.controller;

import com.orchard.common.Result;
import com.orchard.entity.Notification;
import com.orchard.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/** 站内消息通知（所有登录角色） */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public Result<Page<Notification>> list(@RequestAttribute("uid") Long uid,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        return Result.ok(notificationService.list(uid, page, size));
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Object>> unreadCount(@RequestAttribute("uid") Long uid) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("count", notificationService.unreadCount(uid));
        return Result.ok(m);
    }

    @PutMapping("/{id}/read")
    public Result<Void> read(@RequestAttribute("uid") Long uid, @PathVariable Long id) {
        notificationService.markRead(uid, id);
        return Result.ok();
    }

    @PutMapping("/read-all")
    public Result<Void> readAll(@RequestAttribute("uid") Long uid) {
        notificationService.markAllRead(uid);
        return Result.ok();
    }
}
