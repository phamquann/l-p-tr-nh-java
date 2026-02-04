package nhom8.minhquan.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nhom8.minhquan.entities.Invoice;
import nhom8.minhquan.entities.User;
import nhom8.minhquan.services.CartService;
import nhom8.minhquan.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CartService cartService;

    @GetMapping
    public String showProfile(Authentication authentication, Model model) {
        User user = resolveUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "profile";
    }
    
    @GetMapping("/orders")
    public String showMyOrders(Authentication authentication, Model model) {
        User user = resolveUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }
        String email = user.getEmail();
        if (email == null || email.isBlank()) {
            email = user.getUsername();
        }
        List<Invoice> orders = cartService.getInvoicesByCustomerEmail(email);
        
        model.addAttribute("user", user);
        model.addAttribute("orders", orders);
        return "my-orders";
    }
    
    @GetMapping("/orders/{id}")
    public String showOrderDetail(Authentication authentication,
                                  @PathVariable Long id,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        User user = resolveUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }
        
        try {
            Invoice order = cartService.getInvoiceById(id);
            
            // Kiểm tra đơn hàng có thuộc về user này không
            if (!order.getCustomerEmail().equals(user.getEmail())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền xem đơn hàng này!");
                return "redirect:/profile/orders";
            }
            
            model.addAttribute("user", user);
            model.addAttribute("order", order);
            model.addAttribute("orderItems", cartService.getInvoiceItems(id));
            return "order-detail";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng!");
            return "redirect:/profile/orders";
        }
    }

    @PostMapping("/update")
    public String updateProfile(
            Authentication authentication,
            @RequestParam String email,
            @RequestParam String fullName,
            @RequestParam String phone,
            RedirectAttributes redirectAttributes) {
        
        try {
            User user = resolveUser(authentication);
            if (user == null) {
                return "redirect:/login";
            }
            
            // Kiểm tra email đã tồn tại (ngoại trừ email hiện tại)
            if (!email.equals(user.getEmail()) && userService.existsByEmail(email)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Email đã được sử dụng bởi tài khoản khác!");
                return "redirect:/profile";
            }
            
            user.setEmail(email);
            user.setFullName(fullName);
            user.setPhone(phone);
            
            userService.updateProfile(user);
            
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return "redirect:/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(
            Authentication authentication,
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Kiểm tra mật khẩu mới và xác nhận khớp nhau
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới và xác nhận không khớp!");
                return "redirect:/profile";
            }
            
            // Kiểm tra độ dài mật khẩu
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới phải có ít nhất 6 ký tự!");
                return "redirect:/profile";
            }
            
            User user = resolveUser(authentication);
            if (user == null) {
                return "redirect:/login";
            }
            
            boolean success = userService.changePassword(user, currentPassword, newPassword);
            
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu hiện tại không đúng!");
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return "redirect:/profile";
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
