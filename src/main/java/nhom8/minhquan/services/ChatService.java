package nhom8.minhquan.services;

import lombok.RequiredArgsConstructor;
import nhom8.minhquan.entities.Conversation;
import nhom8.minhquan.entities.Message;
import nhom8.minhquan.entities.User;
import nhom8.minhquan.repositories.ConversationRepository;
import nhom8.minhquan.repositories.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {
    
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    
    /**
     * Get or create conversation for a customer
     */
    @Transactional
    public Conversation getOrCreateConversation(User customer) {
        Optional<Conversation> existingConv = conversationRepository.findByCustomerAndStatus(customer, "ACTIVE");
        
        if (existingConv.isPresent()) {
            return existingConv.get();
        }
        
        // Create new conversation
        Conversation conversation = Conversation.builder()
                .customer(customer)
                .status("ACTIVE")
                .unreadCountCustomer(0)
                .unreadCountAdmin(0)
                .build();
        
        return conversationRepository.save(conversation);
    }
    
    /**
     * Get all conversations for admin (ordered by last update)
     */
    public List<Conversation> getAllConversationsForAdmin() {
        return conversationRepository.findAllByOrderByUpdatedAtDesc();
    }
    
    /**
     * Get conversation by ID
     */
    public Optional<Conversation> getConversationById(Long id) {
        return conversationRepository.findById(id);
    }
    
    /**
     * Get all messages in a conversation
     */
    public List<Message> getMessages(Long conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }
    
    /**
     * Send message from customer
     */
    @Transactional
    public Message sendMessageFromCustomer(User customer, String content) {
        Conversation conversation = getOrCreateConversation(customer);
        
        Message message = Message.builder()
                .conversation(conversation)
                .sender(customer)
                .content(content)
                .senderType("CUSTOMER")
                .isRead(false)
                .build();
        
        // Increment unread count for admin
        conversation.setUnreadCountAdmin(conversation.getUnreadCountAdmin() + 1);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        
        return messageRepository.save(message);
    }
    
    /**
     * Send message from admin
     */
    @Transactional
    public Message sendMessageFromAdmin(User admin, Long conversationId, String content) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        
        Message message = Message.builder()
                .conversation(conversation)
                .sender(admin)
                .content(content)
                .senderType("ADMIN")
                .isRead(false)
                .build();
        
        // Set admin for conversation if not set
        if (conversation.getAdmin() == null) {
            conversation.setAdmin(admin);
        }
        
        // Increment unread count for customer
        conversation.setUnreadCountCustomer(conversation.getUnreadCountCustomer() + 1);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        
        return messageRepository.save(message);
    }
    
    /**
     * Mark messages as read for customer
     */
    @Transactional
    public void markAsReadForCustomer(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        
        messageRepository.markAllAsReadByConversationAndSenderType(conversation, "ADMIN");
        conversation.setUnreadCountCustomer(0);
        conversationRepository.save(conversation);
    }
    
    /**
     * Mark messages as read for admin
     */
    @Transactional
    public void markAsReadForAdmin(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        
        messageRepository.markAllAsReadByConversationAndSenderType(conversation, "CUSTOMER");
        conversation.setUnreadCountAdmin(0);
        conversationRepository.save(conversation);
    }
    
    /**
     * Get unread count for customer
     */
    public Integer getUnreadCountForCustomer(User customer) {
        Optional<Conversation> conversation = conversationRepository.findByCustomerAndStatus(customer, "ACTIVE");
        return conversation.map(Conversation::getUnreadCountCustomer).orElse(0);
    }
    
    /**
     * Get total unread conversations for admin
     */
    public Long getTotalUnreadConversationsForAdmin() {
        return conversationRepository.countUnreadConversationsForAdmin();
    }
    
    /**
     * Close conversation
     */
    @Transactional
    public void closeConversation(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        
        conversation.setStatus("CLOSED");
        conversationRepository.save(conversation);
    }
    
    /**
     * Get conversation for customer
     */
    public Optional<Conversation> getConversationForCustomer(User customer) {
        return conversationRepository.findByCustomerAndStatus(customer, "ACTIVE");
    }
}
