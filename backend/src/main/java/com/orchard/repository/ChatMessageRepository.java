package com.orchard.repository;

import com.orchard.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findTop100ByConversationKeyOrderByCreatedAtAsc(String conversationKey);

    /** 会话列表：按最后发言时间倒序 */
    @Query("SELECT c.conversationKey, MAX(c.createdAt) FROM ChatMessage c " +
            "WHERE c.storeId = :storeId GROUP BY c.conversationKey ORDER BY MAX(c.createdAt) DESC")
    List<Object[]> findStoreConversations(@Param("storeId") Long storeId);

    @Query("SELECT c.conversationKey, MAX(c.createdAt) FROM ChatMessage c " +
            "WHERE c.userId = :userId GROUP BY c.conversationKey ORDER BY MAX(c.createdAt) DESC")
    List<Object[]> findUserConversations(@Param("userId") Long userId);

    /** 全平台会话（聊天监控） */
    @Query("SELECT c.conversationKey, MAX(c.createdAt) FROM ChatMessage c " +
            "GROUP BY c.conversationKey ORDER BY MAX(c.createdAt) DESC")
    List<Object[]> findAllConversations();

    /** 未读数（对方发的、我未读的） */
    long countByConversationKeyAndSenderRoleNotAndReadFlagFalse(String conversationKey, String senderRole);

    /** 进入会话时把对方消息置为已读 */
    @Modifying
    @Query("UPDATE ChatMessage c SET c.readFlag = true WHERE c.conversationKey = :key AND c.senderRole <> :role AND c.readFlag = false")
    int markRead(@Param("key") String key, @Param("role") String role);
}
