package nhom8.minhquan.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import nhom8.minhquan.daos.Cart;
import nhom8.minhquan.entities.Book;
import nhom8.minhquan.entities.Invoice;
import nhom8.minhquan.entities.User;
import nhom8.minhquan.services.BookService;
import nhom8.minhquan.services.CartService;
import nhom8.minhquan.services.UserService;
import nhom8.minhquan.services.VoucherService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    
    private final BookService bookService;
    private final CartService cartService;
    private final UserService userService;
    private final VoucherService voucherService;
    
    private User getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) return null;
        try {
            return userService.findByUsername(userDetails.getUsername());
        } catch (Exception e) {
            return null;
        }
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
    
    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        Cart cart = cartService.getCart(session);
        model.addAttribute("cart", cart);
        model.addAttribute("totalItems", cart.getTotalItems());
        model.addAttribute("totalPrice", cart.getTotalPrice());
        return "cart";
    }
    
    @PostMapping("/add/{id}")
    public String addToCart(@PathVariable Long id, 
                           HttpSession session,
                           @AuthenticationPrincipal UserDetails userDetails,
                           RedirectAttributes redirectAttributes) {
        Book book = bookService.getBookById(id).orElse(null);
        if (book == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sách!");
            return "redirect:/books";
        }
        
        User user = getCurrentUser(userDetails);
        cartService.addToCart(session, book, user);
        
        redirectAttributes.addFlashAttribute("successMessage", 
            "Đã thêm \"" + book.getTitle() + "\" vào giỏ hàng!");
        return "redirect:/books";
    }
    
    @PostMapping("/update/{id}")
    public String updateQuantity(@PathVariable Long id,
                                 @RequestParam int quantity,
                                 HttpSession session,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);
        cartService.updateQuantity(session, id, quantity, user);
        
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật giỏ hàng thành công!");
        return "redirect:/cart";
    }
    
    @GetMapping("/remove/{id}")
    public String removeFromCart(@PathVariable Long id,
                                HttpSession session,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);
        cartService.removeFromCart(session, id, user);
        
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sách khỏi giỏ hàng!");
        return "redirect:/cart";
    }
    
    @GetMapping("/clear")
    public String clearCart(HttpSession session, 
                           @AuthenticationPrincipal UserDetails userDetails,
                           RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);
        cartService.clearCart(session, user);
        
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa toàn bộ giỏ hàng!");
        return "redirect:/cart";
    }
    
    /**
     * Hiển thị trang checkout với form nhập thông tin giao hàng
     */
    @GetMapping("/checkout")
    public String showCheckout(HttpSession session, 
                               Authentication authentication,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Cart cart = cartService.getCart(session);
        
        if (cart.getItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng trống! Hãy thêm sản phẩm trước khi thanh toán.");
            return "redirect:/cart";
        }
        
        // Lấy thông tin user nếu đã đăng nhập
        User user = resolveUser(authentication);
        
        model.addAttribute("cart", cart);
        model.addAttribute("totalItems", cart.getTotalItems());
        model.addAttribute("totalPrice", cart.getTotalPrice());
        model.addAttribute("user", user);
        if (!model.containsAttribute("voucherCode")) {
            model.addAttribute("voucherCode", "");
        }
        
        return "checkout";
    }
    
    /**
     * Xử lý đặt hàng sau khi nhập thông tin giao hàng
     */
    @PostMapping("/checkout")
    public String processCheckout(HttpSession session, 
                          @RequestParam String customerName,
                          @RequestParam String customerEmail,
                          @RequestParam String customerPhone,
                          @RequestParam String customerAddress,
                          @RequestParam(required = false) String orderNote,
                          @RequestParam(required = false, defaultValue = "COD") String paymentMethod,
                          @RequestParam(required = false) String voucherCode,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {
        Cart cart = cartService.getCart(session);
        
        if (cart.getItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng trống!");
            return "redirect:/cart";
        }
        
        // Validate required fields
        if (customerName == null || customerName.trim().isEmpty() ||
            customerEmail == null || customerEmail.trim().isEmpty() ||
            customerPhone == null || customerPhone.trim().isEmpty() ||
            customerAddress == null || customerAddress.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng điền đầy đủ thông tin giao hàng!");
            return "redirect:/cart/checkout";
        }
        
        try {
            User user = resolveUser(authentication);

            if (user != null && user.getEmail() != null && !user.getEmail().isBlank()) {
                customerEmail = user.getEmail();
            }

            Double discountAmount = 0.0;
            Double finalTotal = cart.getTotalPrice();
            String appliedVoucherCode = null;
            var appliedVoucher = (nhom8.minhquan.entities.Voucher) null;

            if (voucherCode != null && !voucherCode.trim().isEmpty()) {
                appliedVoucherCode = voucherCode.trim();
                var voucherOpt = voucherService.validateAndApplyVoucher(appliedVoucherCode, cart.getTotalPrice());
                if (voucherOpt.isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Mã voucher không hợp lệ hoặc không đủ điều kiện áp dụng.");
                    redirectAttributes.addFlashAttribute("voucherCode", appliedVoucherCode);
                    return "redirect:/cart/checkout";
                }
                appliedVoucher = voucherOpt.get();
                discountAmount = appliedVoucher.calculateDiscount(cart.getTotalPrice());
                finalTotal = Math.max(0.0, cart.getTotalPrice() - discountAmount);
            }
            
            // Tạo hóa đơn và lưu vào database
            Invoice invoice = cartService.createInvoiceFromCart(
                session,
                customerName.trim(),
                customerEmail.trim(),
                customerPhone.trim(),
                customerAddress.trim(),
                user,
                appliedVoucherCode,
                discountAmount,
                finalTotal
            );

            if (appliedVoucher != null) {
                voucherService.incrementUsageCount(appliedVoucher.getId());
            }
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "🎉 Đặt hàng thành công! Mã đơn hàng: #" + invoice.getId() + ". Cảm ơn bạn đã mua hàng tại HUTECH Bookstore!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Lỗi khi đặt hàng: " + e.getMessage());
            return "redirect:/cart/checkout";
        }
        
        return "redirect:/profile/orders";
    }

    /**
     * Áp dụng voucher (preview giảm giá) khi checkout
     */
    @PostMapping("/checkout/apply-voucher")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> applyVoucher(HttpSession session,
                                                            @RequestParam String voucherCode) {
        Cart cart = cartService.getCart(session);
        Map<String, Object> response = new HashMap<>();

        double total = cart.getTotalPrice();
        double finalTotal = total;
        double discount = 0.0;

        if (voucherCode == null || voucherCode.trim().isEmpty()) {
            response.put("valid", false);
            response.put("message", "Vui lòng nhập mã voucher.");
            response.put("finalTotalFormatted", formatCurrency(finalTotal));
            return ResponseEntity.ok(response);
        }

        var voucherOpt = voucherService.validateAndApplyVoucher(voucherCode.trim(), total);
        if (voucherOpt.isEmpty()) {
            response.put("valid", false);
            response.put("message", "Mã voucher không hợp lệ hoặc không đủ điều kiện áp dụng.");
            response.put("finalTotalFormatted", formatCurrency(finalTotal));
            return ResponseEntity.ok(response);
        }

        var voucher = voucherOpt.get();
        discount = voucher.calculateDiscount(total);
        finalTotal = Math.max(0.0, total - discount);

        response.put("valid", true);
        response.put("message", "Áp dụng voucher thành công!");
        response.put("discountAmount", discount);
        response.put("discountFormatted", formatCurrency(discount));
        response.put("finalTotal", finalTotal);
        response.put("finalTotalFormatted", formatCurrency(finalTotal));

        return ResponseEntity.ok(response);
    }

    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(Math.round(amount)) + "đ";
    }
}
