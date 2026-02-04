package nhom8.minhquan.controllers.rest;

import lombok.RequiredArgsConstructor;
import nhom8.minhquan.dto.ApiResponse;
import nhom8.minhquan.dto.UserInfoDTO;
import nhom8.minhquan.entities.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller để demo Authorization trong API
 * Các endpoint này cho thấy cách phân quyền hoạt động
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthRestController {
    
    /**
     * GET /api/auth/me - Lấy thông tin user hiện tại
     * Endpoint này cho phép TẤT CẢ user đã đăng nhập truy cập
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoDTO>> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Lấy roles của user
        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        
        // Xác định permissions dựa trên roles
        List<String> permissions = getPermissionsByRoles(roles);
        
        UserInfoDTO userInfo = UserInfoDTO.builder()
                .username(auth.getName())
                .email(auth.getName() + "@hutech.edu.vn") // Demo purposes
                .fullName(auth.getName().toUpperCase())
                .roles(roles)
                .permissions(permissions)
                .build();
        
        return ResponseEntity.ok(
            ApiResponse.success("Lấy thông tin user thành công", userInfo)
        );
    }
    
    /**
     * GET /api/auth/admin-only - Endpoint CHỈ ADMIN truy cập được
     * Demo: Authorization kiểm tra role
     */
    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> adminOnly() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        String message = String.format(
            "🔐 Xin chào ADMIN '%s'! Bạn đã được ủy quyền truy cập endpoint này. " +
            "USER thường KHÔNG THỂ truy cập được.", 
            auth.getName()
        );
        
        return ResponseEntity.ok(
            ApiResponse.success(message, "ADMIN_ACCESS_GRANTED")
        );
    }
    
    /**
     * GET /api/auth/user-only - Endpoint CHỈ USER truy cập được
     * Demo: Authorization cho role USER
     */
    @GetMapping("/user-only")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> userOnly() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        String message = String.format(
            "👤 Xin chào USER '%s'! Bạn có quyền USER. " +
            "Endpoint này dành cho USER, ADMIN KHÔNG truy cập được.", 
            auth.getName()
        );
        
        return ResponseEntity.ok(
            ApiResponse.success(message, "USER_ACCESS_GRANTED")
        );
    }
    
    /**
     * GET /api/auth/any-authenticated - Endpoint cho TẤT CẢ user đã xác thực
     * Demo: Chỉ cần authenticated, không quan tâm role
     */
    @GetMapping("/any-authenticated")
    public ResponseEntity<ApiResponse<List<String>>> anyAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        
        String message = String.format(
            "✅ Xin chào '%s'! Bạn có roles: %s. " +
            "Endpoint này KHÔNG kiểm tra role, chỉ cần đăng nhập.", 
            auth.getName(),
            String.join(", ", roles)
        );
        
        return ResponseEntity.ok(
            ApiResponse.success(message, roles)
        );
    }
    
    /**
     * GET /api/auth/permissions - Xem permissions của user hiện tại
     * Demo: Hiển thị các quyền user có dựa trên role
     */
    @GetMapping("/permissions")
    public ResponseEntity<ApiResponse<List<String>>> getMyPermissions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        
        List<String> permissions = getPermissionsByRoles(roles);
        
        return ResponseEntity.ok(
            ApiResponse.success(
                String.format("User '%s' có %d permissions", auth.getName(), permissions.size()),
                permissions
            )
        );
    }
    
    /**
     * Helper method: Map roles sang permissions
     */
    private List<String> getPermissionsByRoles(List<String> roles) {
        // ADMIN có TẤT CẢ quyền
        if (roles.contains("ROLE_ADMIN")) {
            return Arrays.asList(
                "book:read",
                "book:create",
                "book:update",
                "book:delete",
                "category:read",
                "category:create",
                "category:update",
                "category:delete",
                "user:read",
                "user:manage"
            );
        }
        
        // USER chỉ có quyền đọc
        if (roles.contains("ROLE_USER")) {
            return Arrays.asList(
                "book:read",
                "category:read"
            );
        }
        
        return Arrays.asList();
    }
}
