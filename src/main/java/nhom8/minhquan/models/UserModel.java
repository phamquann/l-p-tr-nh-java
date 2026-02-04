package nhom8.minhquan.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Model cho User API responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserModel {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private Boolean enabled;
    private List<String> roles;
    private List<String> permissions;
}
