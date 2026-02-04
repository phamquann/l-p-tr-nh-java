package nhom8.minhquan.viewmodels;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ViewModel cho Login - sử dụng trong form đăng nhập
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginViewModel {
    private String username;
    private String password;
    private Boolean rememberMe;
    private String errorMessage;
    private String successMessage;
}
