package nhom8.minhquan.controllers;

import lombok.RequiredArgsConstructor;
import nhom8.minhquan.entities.Category;
import nhom8.minhquan.entities.Conversation;
import nhom8.minhquan.entities.Invoice;
import nhom8.minhquan.entities.Message;
import nhom8.minhquan.entities.User;
import nhom8.minhquan.services.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    
    private final BookService bookService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final CartService cartService;
    private final ReportService reportService;
    private final VoucherService voucherService;
    private final ChatService chatService;
    
    /**
     * Redirect /admin to /admin/dashboard
     */
    @GetMapping("")
    public String adminRoot() {
        return "redirect:/admin/dashboard";
    }
    
    /**
     * Dashboard - Tổng quan
     */
    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Dashboard - Tổng quan");
        model.addAttribute("totalBooks", bookService.countBooks());
        model.addAttribute("totalCategories", categoryService.getAllCategories().size());
        model.addAttribute("totalUsers", userService.getAllUsers().size());
        model.addAttribute("totalVouchers", voucherService.countVouchers());
        model.addAttribute("validVouchers", voucherService.countValidVouchers());
        
        // Thống kê đơn hàng
        List<Invoice> allOrders = cartService.getAllInvoices();
        model.addAttribute("totalOrders", allOrders.size());
        model.addAttribute("pendingOrders", cartService.countInvoicesByStatus("PENDING"));
        model.addAttribute("confirmedOrders", cartService.countInvoicesByStatus("CONFIRMED"));
        model.addAttribute("shippingOrders", cartService.countInvoicesByStatus("SHIPPING"));
        model.addAttribute("completedOrders", cartService.countInvoicesByStatus("COMPLETED"));
        model.addAttribute("cancelledOrders", cartService.countInvoicesByStatus("CANCELLED"));
        
        model.addAttribute("recentBooks", bookService.getAllBooks().stream().limit(5).toList());
        model.addAttribute("recentOrders", allOrders.stream().limit(5).toList());
        
        // Chat unread count
        model.addAttribute("totalUnreadCount", chatService.getTotalUnreadConversationsForAdmin());
        
        return "admin/dashboard";
    }
    
    /**
     * Quản lý người dùng
     */
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("pageTitle", "Quản lý người dùng");
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }
    
    /**
     * Toggle trạng thái user (enable/disable)
     */
    @GetMapping("/users/toggle/{id}")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getAllUsers().stream()
                    .filter(u -> u.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
            user.setEnabled(!user.isEnabled());
            userService.save(user);
            String status = user.isEnabled() ? "kích hoạt" : "khóa";
            redirectAttributes.addFlashAttribute("successMessage", "Đã " + status + " tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    
    /**
     * Xóa người dùng
     */
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa người dùng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    
    /**
     * Quản lý sản phẩm (books)
     */
    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("pageTitle", "Quản lý sản phẩm");
        model.addAttribute("books", bookService.getAllBooks());
        return "admin/products";
    }
    
    /**
     * Quản lý đơn hàng
     */
    @GetMapping("/orders")
    public String orders(Model model, @RequestParam(required = false) String status) {
        model.addAttribute("pageTitle", "Quản lý đơn hàng");
        
        List<Invoice> orders;
        if (status != null && !status.isEmpty()) {
            orders = cartService.getInvoicesByStatus(status);
        } else {
            orders = cartService.getAllInvoices();
        }
        
        model.addAttribute("orders", orders);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("pendingCount", cartService.countInvoicesByStatus("PENDING"));
        model.addAttribute("confirmedCount", cartService.countInvoicesByStatus("CONFIRMED"));
        model.addAttribute("shippingCount", cartService.countInvoicesByStatus("SHIPPING"));
        model.addAttribute("completedCount", cartService.countInvoicesByStatus("COMPLETED"));
        model.addAttribute("cancelledCount", cartService.countInvoicesByStatus("CANCELLED"));
        
        return "admin/orders";
    }
    
    /**
     * Xem chi tiết đơn hàng
     */
    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "Chi tiết đơn hàng #" + id);
        model.addAttribute("order", cartService.getInvoiceById(id));
        model.addAttribute("orderItems", cartService.getInvoiceItems(id));
        return "admin/order-detail";
    }
    
    /**
     * Cập nhật trạng thái đơn hàng
     */
    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id, 
                                   @RequestParam String status,
                                   RedirectAttributes redirectAttributes) {
        try {
            cartService.updateInvoiceStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái đơn hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }
    
    /**
     * Quản lý danh mục với tìm kiếm
     */
    @GetMapping("/categories")
    public String categories(Model model, 
                           @RequestParam(defaultValue = "") String search,
                           @RequestParam(defaultValue = "0") int page) {
        model.addAttribute("pageTitle", "Quản lý danh mục");
        
        List<Category> allCategories = categoryService.getAllCategories();
        List<Category> filteredCategories = allCategories;
        
        // Tìm kiếm
        if (!search.isEmpty()) {
            filteredCategories = allCategories.stream()
                .filter(cat -> cat.getName().toLowerCase().contains(search.toLowerCase()))
                .collect(java.util.stream.Collectors.toList());
        }
        
        // Phân trang đơn giản
        int pageSize = 10;
        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, filteredCategories.size());
        
        List<Category> pagedCategories = filteredCategories.subList(
            Math.min(startIndex, filteredCategories.size()), endIndex);
        
        model.addAttribute("categories", pagedCategories);
        model.addAttribute("allCategories", allCategories);
        model.addAttribute("search", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (int) Math.ceil((double) filteredCategories.size() / pageSize));
        model.addAttribute("newCategory", new Category());
        
        return "admin/categories";
    }
    
    /**
     * Thêm danh mục mới
     */
    @PostMapping("/categories/add")
    public String addCategory(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        try {
            categoryService.saveCategory(category);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm danh mục thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }
    
    /**
     * Cập nhật danh mục
     */
    @PostMapping("/categories/edit/{id}")
    public String updateCategory(@PathVariable Long id, @RequestParam String name, RedirectAttributes redirectAttributes) {
        try {
            Category category = categoryService.getCategoryById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục"));
            category.setName(name);
            categoryService.saveCategory(category);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật danh mục thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }
    
    /**
     * Xóa danh mục
     */
    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa danh mục thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    /**
     * Thống kê - Báo cáo
     */
    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("pageTitle", "Thống kê - Báo cáo");
        
        // Thống kê tổng quan
        model.addAttribute("overallStats", reportService.getOverallStatistics());
        
        // Thống kê 7 ngày gần nhất
        model.addAttribute("last7DaysStats", reportService.getLast7DaysStatistics());
        
        // Thống kê theo danh mục
        model.addAttribute("categoryStats", reportService.getCategoryStatistics());
        
        // Top sách bán chạy
        model.addAttribute("topBooks", reportService.getTopSellingBooks(5));
        
        // Thống kê trạng thái đơn hàng
        model.addAttribute("orderStatusStats", reportService.getOrderStatusStatistics());
        
        // Thống kê người dùng mới
        model.addAttribute("newUsersStats", reportService.getNewUsersLast7Days());
        
        return "admin/reports";
    }
    
    /**
     * Cài đặt hệ thống
     */
    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("pageTitle", "Cài đặt hệ thống");
        return "admin/settings";
    }
    
    /**
     * Trò chuyện với khách hàng
     */
    @GetMapping("/chat")
    public String chat(Model model, @RequestParam(required = false) Long conversationId) {
        model.addAttribute("pageTitle", "Trò chuyện với khách hàng");
        
        // Get all conversations
        List<Conversation> conversations = chatService.getAllConversationsForAdmin();
        model.addAttribute("conversations", conversations);
        
        // Get selected conversation or first one
        Conversation selectedConversation = null;
        if (conversationId != null) {
            selectedConversation = chatService.getConversationById(conversationId).orElse(null);
        } else if (!conversations.isEmpty()) {
            selectedConversation = conversations.get(0);
        }
        
        if (selectedConversation != null) {
            List<Message> messages = chatService.getMessages(selectedConversation.getId());
            model.addAttribute("selectedConversation", selectedConversation);
            model.addAttribute("messages", messages);
            
            // Mark messages as read
            chatService.markAsReadForAdmin(selectedConversation.getId());
        }
        
        // Get total unread count
        Long unreadCount = chatService.getTotalUnreadConversationsForAdmin();
        model.addAttribute("totalUnreadCount", unreadCount);
        
        return "admin/chat";
    }
    
    /**
     * Send message from admin (AJAX)
     */
    @PostMapping("/api/chat/send")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendAdminMessage(
            @RequestParam Long conversationId,
            @RequestParam String message,
            Authentication authentication) {
        
        User admin = userService.findByUsername(authentication.getName());
        Message sentMessage = chatService.sendMessageFromAdmin(admin, conversationId, message);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", toMessageDto(sentMessage));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get messages for a conversation (AJAX)
     */
    @GetMapping("/api/chat/messages/{conversationId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getConversationMessages(@PathVariable Long conversationId) {
        List<Message> messages = chatService.getMessages(conversationId);
        
        // Mark as read
        chatService.markAsReadForAdmin(conversationId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("messages", messages.stream().map(this::toMessageDto).collect(Collectors.toList()));
        
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> toMessageDto(Message message) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", message.getId());
        dto.put("content", message.getContent());
        dto.put("senderType", message.getSenderType());
        dto.put("createdAt", message.getCreatedAt());
        return dto;
    }
}
