package nhom8.minhquan.models;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model cho Login request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestModel {
    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String username;
    
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
    
    private Boolean rememberMe;
}
