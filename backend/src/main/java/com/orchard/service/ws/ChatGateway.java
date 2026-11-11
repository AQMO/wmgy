package com.orchard.service.ws;

import com.orchard.entity.ChatMessage;
import com.orchard.service.ChatService;
import org.springframework.stereotype.Service;

/** WS → 聊天业务桥接：ChatWsHandler 经此调用 ChatService，避免循环依赖 */
@Service
public class ChatGateway {

    private final ChatService chatService;

    public ChatGateway(ChatService chatService) {
        this.chatService = chatService;
    }

    /** @return null 表示发送失败（含业务异常） */
    public ChatMessage sendFromWs(Long uid, String role, Long storeId, Long peerUserId, String content) {
        try {
            return chatService.send(uid, role, storeId, peerUserId, content);
        } catch (Exception e) {
            return null;
        }
    }
}
