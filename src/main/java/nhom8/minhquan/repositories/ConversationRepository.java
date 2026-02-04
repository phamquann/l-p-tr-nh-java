package nhom8.minhquan.repositories;

import nhom8.minhquan.entities.Conversation;
import nhom8.minhquan.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    // Find conversation by customer
    Optional<Conversation> findByCustomerAndStatus(User customer, String status);
    
    // Find all conversations for a customer
    List<Conversation> findByCustomerOrderByUpdatedAtDesc(User customer);
    
    // Find all active conversations ordered by last update
    List<Conversation> findByStatusOrderByUpdatedAtDesc(String status);
    
    // Find all conversations (for admin)
    List<Conversation> findAllByOrderByUpdatedAtDesc();
    
    // Count unread conversations for admin
    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.unreadCountAdmin > 0 AND c.status = 'ACTIVE'")
    Long countUnreadConversationsForAdmin();
    
    // Find conversation by customer ID
    Optional<Conversation> findByCustomerId(Long customerId);
    
    // Check if conversation exists for customer
    boolean existsByCustomerAndStatus(User customer, String status);
}
