package nhom8.minhquan.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model cho Login response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseModel {
    private String token;
    private String tokenType;
    private Long expiresIn;
    private UserModel user;
}
