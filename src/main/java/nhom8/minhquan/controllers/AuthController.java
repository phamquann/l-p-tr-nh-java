package nhom8.minhquan.controllers;

import lombok.RequiredArgsConstructor;
import nhom8.minhquan.entities.Role;
import nhom8.minhquan.entities.User;
import nhom8.minhquan.services.UserService;
import nhom8.minhquan.repositories.IRoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    private final IRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (error != null) {
            model.addAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không đúng!");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Đăng xuất thành công!");
        }
        return "auth/login";
    }
    
    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("errorMessage", "Bạn không có quyền truy cập trang này!");
        return "auth/access-denied";
    }
    
    /**
     * GET /register - Hiển thị form đăng ký
     */
    @GetMapping("/register")
    public String showRegisterForm() {
        return "auth/register";
    }
    
    /**
     * POST /register - Xử lý đăng ký tài khoản
     */
    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String fullName,
            @RequestParam(required = false) String phone,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        try {
            // Validate password match
            if (!password.equals(confirmPassword)) {
                model.addAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
                return "auth/register";
            }
            
            // Check if username exists
            if (userService.existsByUsername(username)) {
                model.addAttribute("errorMessage", "Tên đăng nhập đã tồn tại!");
                return "auth/register";
            }
            
            // Check if email exists
            if (userService.existsByEmail(email)) {
                model.addAttribute("errorMessage", "Email đã được sử dụng!");
                return "auth/register";
            }
            
            // Create new user
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setFullName(fullName);
            user.setPhone(phone);
            user.setPassword(passwordEncoder.encode(password));
            user.setEnabled(true);
            
            // Assign USER role
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Role USER not found!"));
            
            Set<Role> roles = new HashSet<>();
            roles.add(userRole);
            user.setRoles(roles);
            
            // Save user
            userService.save(user);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Đăng ký thành công! Vui lòng đăng nhập.");
            
            return "redirect:/login";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Đăng ký thất bại: " + e.getMessage());
            return "auth/register";
        }
    }
}
