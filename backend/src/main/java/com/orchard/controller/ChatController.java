package com.orchard.controller;

import com.orchard.common.Result;
import com.orchard.entity.ChatMessage;
import com.orchard.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /** 会话列表（买家=与各店铺，商家=与各买家） */
    @GetMapping("/api/chat/conversations")
    public Result<List<Map<String, Object>>> conversations(@RequestAttribute("uid") Long uid,
                                                           @RequestAttribute("role") String role) {
        if ("MERCHANT".equals(role)) return Result.ok(chatService.merchantConversations(uid));
        return Result.ok(chatService.userConversations(uid));
    }

    /** 消息历史（进入会话自动置对方消息已读） */
    @GetMapping("/api/chat/messages")
    public Result<Map<String, Object>> messages(@RequestAttribute("uid") Long uid,
                                                @RequestAttribute("role") String role,
                                                @RequestParam String conversationKey) {
        return Result.ok(chatService.history(uid, role, conversationKey));
    }

    /** 发送消息：买家传 storeId；商家传对方 userId */
    @PostMapping("/api/chat/send")
    public Result<ChatMessage> send(@RequestAttribute("uid") Long uid,
                                    @RequestAttribute("role") String role,
                                    @RequestBody Map<String, Object> body) {
        Long storeId = body.get("storeId") == null ? null : Long.valueOf(String.valueOf(body.get("storeId")));
        Long peerUserId = body.get("userId") == null ? null : Long.valueOf(String.valueOf(body.get("userId")));
        String content = (String) body.get("content");
        return Result.ok(chatService.send(uid, role, storeId, peerUserId, content));
    }

    // ---------- 平台聊天监控（PRD 5.4） ----------

    @GetMapping("/api/admin/chats")
    public Result<List<Map<String, Object>>> allConversations() {
        return Result.ok(chatService.allConversations());
    }

    @GetMapping("/api/admin/chats/messages")
    public Result<Map<String, Object>> adminMessages(@RequestParam String conversationKey) {
        return Result.ok(chatService.history(null, "ADMIN", conversationKey));
    }
}
