package nhom8.minhquan.controllers;

import lombok.RequiredArgsConstructor;
import nhom8.minhquan.entities.Conversation;
import nhom8.minhquan.entities.Message;
import nhom8.minhquan.entities.User;
import nhom8.minhquan.services.ChatService;
import nhom8.minhquan.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService chatService;
    private final UserService userService;
    
    /**
     * Customer chat page
     */
    @GetMapping("/chat")
    public String customerChat(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        User customer = resolveUser(authentication);
        if (customer == null) {
            return "redirect:/login";
        }
        Optional<Conversation> conversation = chatService.getConversationForCustomer(customer);
        
        if (conversation.isPresent()) {
            List<Message> messages = chatService.getMessages(conversation.get().getId());
            model.addAttribute("conversation", conversation.get());
            model.addAttribute("messages", messages);
            
            // Mark messages as read
            chatService.markAsReadForCustomer(conversation.get().getId());
        }
        
        return "chat";
    }
    
    /**
     * Get customer's messages (AJAX)
     */
    @GetMapping("/api/chat/messages")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMessages(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        User customer = resolveUser(authentication);
        if (customer == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<Conversation> conversation = chatService.getConversationForCustomer(customer);
        
        Map<String, Object> response = new HashMap<>();
        
        if (conversation.isPresent()) {
            List<Message> messages = chatService.getMessages(conversation.get().getId());
            response.put("messages", messages.stream().map(this::toMessageDto).collect(Collectors.toList()));
            response.put("conversationId", conversation.get().getId());
            
            // Mark as read
            chatService.markAsReadForCustomer(conversation.get().getId());
        } else {
            response.put("messages", List.of());
            response.put("conversationId", null);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Send message from customer (AJAX)
     */
    @PostMapping("/api/chat/send")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendMessage(
            @RequestParam String message,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        
        User customer = resolveUser(authentication);
        if (customer == null) {
            return ResponseEntity.status(401).build();
        }
        Message sentMessage = chatService.sendMessageFromCustomer(customer, message);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", toMessageDto(sentMessage));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get unread count (AJAX)
     */
    @GetMapping("/api/chat/unread-count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUnreadCount(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.ok(Map.of("count", 0));
        }
        
        User customer = resolveUser(authentication);
        if (customer == null) {
            return ResponseEntity.ok(Map.of("count", 0));
        }
        Integer unreadCount = chatService.getUnreadCountForCustomer(customer);
        
        return ResponseEntity.ok(Map.of("count", unreadCount));
    }

    private Map<String, Object> toMessageDto(Message message) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", message.getId());
        dto.put("content", message.getContent());
        dto.put("senderType", message.getSenderType());
        dto.put("createdAt", message.getCreatedAt());
        return dto;
    }

    private User resolveUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userService.findByUsername(userDetails.getUsername());
        }
        if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            if (email != null) {
                return userService.findByEmail(email);
            }
        }

        String name = authentication.getName();
        if (name != null) {
            return userService.findByUsername(name);
        }

        return null;
    }
}
