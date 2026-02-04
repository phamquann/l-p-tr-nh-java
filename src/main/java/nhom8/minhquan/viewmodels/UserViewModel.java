package nhom8.minhquan.viewmodels;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ViewModel cho User - sử dụng trong Web MVC Controllers
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserViewModel {
    private Long id;
    
    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 50, message = "Tên đăng nhập phải từ 3-50 ký tự")
    private String username;
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
    
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;
    
    private String phone;
    
    // Password chỉ dùng khi tạo mới hoặc đổi password
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;
    
    private String confirmPassword;
    
    // Roles
    private List<String> roles;
    
    // Account status
    private Boolean enabled;
    
    // Display properties
    public String getRolesDisplay() {
        if (roles == null || roles.isEmpty()) {
            return "Chưa có role";
        }
        return String.join(", ", roles);
    }
    
    public String getStatusDisplay() {
        return (enabled != null && enabled) ? "Hoạt động" : "Bị khóa";
    }
}
