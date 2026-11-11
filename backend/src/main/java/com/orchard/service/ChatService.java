package com.orchard.service;

import com.orchard.common.BizException;
import com.orchard.entity.ChatMessage;
import com.orchard.entity.Store;
import com.orchard.entity.User;
import com.orchard.repository.ChatMessageRepository;
import com.orchard.repository.StoreRepository;
import com.orchard.repository.UserRepository;
import com.orchard.service.ws.WsPushService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/** 聊天服务：买家↔商家一对一，WebSocket 实时推送（离线由历史拉取兜底）；平台可监控全部会话 */
@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final WsPushService wsPushService;

    public ChatService(ChatMessageRepository chatMessageRepository, StoreRepository storeRepository,
                       UserRepository userRepository, WsPushService wsPushService) {
        this.chatMessageRepository = chatMessageRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.wsPushService = wsPushService;
    }

    /** 消费者会话列表（与各店铺的最近会话） */
    public List<Map<String, Object>> userConversations(Long uid) {
        List<Object[]> rows = chatMessageRepository.findUserConversations(uid);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) {
            String key = (String) r[0];
            Long storeId = parseStoreId(key);
            ChatMessage last = lastMessage(key);
            Map<String, Object> m = convBase(key, last);
            m.put("storeId", storeId);
            m.put("peerName", storeRepository.findById(storeId).map(Store::getName).orElse("果园店铺"));
            m.put("unread", chatMessageRepository.countByConversationKeyAndSenderRoleNotAndReadFlagFalse(key, "USER"));
            list.add(m);
        }
        return list;
    }

    /** 商家会话列表（与各买家的最近会话） */
    public List<Map<String, Object>> merchantConversations(Long uid) {
        Store s = storeRepository.findByOwnerId(uid)
                .orElseThrow(() -> new BizException(40004, "尚未开通店铺"));
        List<Object[]> rows = chatMessageRepository.findStoreConversations(s.getId());
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) {
            String key = (String) r[0];
            Long buyerId = parseUserId(key);
            ChatMessage last = lastMessage(key);
            Map<String, Object> m = convBase(key, last);
            m.put("userId", buyerId);
            m.put("peerName", userRepository.findById(buyerId)
                    .map(u -> u.getNickname() != null ? u.getNickname() : u.getUsername()).orElse("买家"));
            m.put("unread", chatMessageRepository.countByConversationKeyAndSenderRoleNotAndReadFlagFalse(key, "MERCHANT"));
            list.add(m);
        }
        return list;
    }

    /** 平台聊天监控：全量会话（PRD 5.4 纠纷取证） */
    public List<Map<String, Object>> allConversations() {
        List<Object[]> rows = chatMessageRepository.findAllConversations();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] r : rows) {
            String key = (String) r[0];
            ChatMessage last = lastMessage(key);
            Map<String, Object> m = convBase(key, last);
            m.put("userId", parseUserId(key));
            m.put("storeId", parseStoreId(key));
            m.put("buyerName", userRepository.findById(parseUserId(key))
                    .map(u -> u.getNickname() != null ? u.getNickname() : u.getUsername()).orElse("买家"));
            m.put("storeName", storeRepository.findById(parseStoreId(key)).map(Store::getName).orElse("果园店铺"));
            list.add(m);
        }
        return list;
    }

    /** 消息历史（进入会话即把对方消息置为已读），带会话归属校验 */
    @Transactional
    public Map<String, Object> history(Long uid, String role, String conversationKey) {
        Long userId = parseUserId(conversationKey);
        Long storeId = parseStoreId(conversationKey);
        if ("USER".equals(role)) {
            if (!userId.equals(uid)) throw new BizException(BizException.FORBIDDEN, "无权查看该会话");
        } else if ("MERCHANT".equals(role)) {
            Store s = storeRepository.findByOwnerId(uid)
                    .orElseThrow(() -> new BizException(40004, "尚未开通店铺"));
            if (!s.getId().equals(storeId)) throw new BizException(BizException.FORBIDDEN, "无权查看该会话");
        } else if (!"ADMIN".equals(role)) {
            throw new BizException(BizException.FORBIDDEN, "无权查看该会话");
        }
        chatMessageRepository.markRead(conversationKey, role);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("messages", chatMessageRepository.findTop100ByConversationKeyOrderByCreatedAtAsc(conversationKey));
        m.put("userId", userId);
        m.put("storeId", storeId);
        return m;
    }

    /** 发送消息。买家传 storeId，商家传对方 userId */
    @Transactional
    public ChatMessage send(Long uid, String role, Long storeIdParam, Long peerUserId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BizException("消息内容不能为空");
        }
        if (content.length() > 500) {
            throw new BizException("消息内容过长");
        }
        Long userId;
        Long storeId;
        String senderRole;
        if ("USER".equals(role)) {
            userId = uid;
            storeId = storeIdParam;
            senderRole = "USER";
            Store s = storeRepository.findById(storeId).orElseThrow(() -> new BizException(40004, "店铺不存在"));
            if (!"ACTIVE".equals(s.getStatus())) throw new BizException("店铺暂不可联系");
        } else if ("MERCHANT".equals(role)) {
            Store s = storeRepository.findByOwnerId(uid)
                    .orElseThrow(() -> new BizException(40004, "尚未开通店铺"));
            storeId = s.getId();
            userId = peerUserId;
            senderRole = "MERCHANT";
        } else {
            throw new BizException(BizException.FORBIDDEN, "管理员不参与聊天，请使用监控页查看");
        }
        ChatMessage msg = new ChatMessage();
        msg.setConversationKey("u" + userId + "_s" + storeId);
        msg.setUserId(userId);
        msg.setStoreId(storeId);
        msg.setSenderRole(senderRole);
        msg.setContent(content.trim());
        ChatMessage saved = chatMessageRepository.save(msg);
        // WebSocket 实时双推：买家 + 店主（离线则由历史拉取兜底）
        wsPushService.pushChat(userId, saved);
        storeRepository.findById(storeId).ifPresent(s -> wsPushService.pushChat(s.getOwnerId(), saved));
        return saved;
    }

    private Map<String, Object> convBase(String key, ChatMessage last) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("conversationKey", key);
        m.put("lastContent", last != null ? last.getContent() : "");
        m.put("lastTime", last != null ? last.getCreatedAt() : null);
        return m;
    }

    private ChatMessage lastMessage(String key) {
        List<ChatMessage> l = chatMessageRepository.findTop100ByConversationKeyOrderByCreatedAtAsc(key);
        return l.isEmpty() ? null : l.get(l.size() - 1);
    }

    private Long parseUserId(String key) {
        try {
            return Long.parseLong(key.substring(1, key.indexOf("_s")));
        } catch (Exception e) {
            throw new BizException("会话标识无效");
        }
    }

    private Long parseStoreId(String key) {
        try {
            return Long.parseLong(key.substring(key.indexOf("_s") + 2));
        } catch (Exception e) {
            throw new BizException("会话标识无效");
        }
    }
}
