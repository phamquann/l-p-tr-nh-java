package nhom8.minhquan.repositories;

import nhom8.minhquan.entities.Conversation;
import nhom8.minhquan.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    // Find all messages in a conversation ordered by time
    List<Message> findByConversationOrderByCreatedAtAsc(Conversation conversation);
    
    // Find all messages in a conversation by ID
    List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
    
    // Count unread messages in a conversation for a specific sender type
    Long countByConversationAndIsReadFalseAndSenderTypeNot(Conversation conversation, String senderType);
    
    // Mark all messages as read in a conversation
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.conversation = :conversation AND m.senderType = :senderType")
    void markAllAsReadByConversationAndSenderType(@Param("conversation") Conversation conversation, @Param("senderType") String senderType);
    
    // Get latest message in conversation
    Message findFirstByConversationOrderByCreatedAtDesc(Conversation conversation);
}
