package nhom8.minhquan.services;

import lombok.RequiredArgsConstructor;
import nhom8.minhquan.entities.Role;
import nhom8.minhquan.entities.User;
import nhom8.minhquan.repositories.IRoleRepository;
import nhom8.minhquan.repositories.IUserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        // Lấy thông tin từ Google
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        
        // Kiểm tra user đã tồn tại chưa
        Optional<User> existingUser = userRepository.findByEmail(email);
        User appUser;
        
        if (existingUser.isEmpty()) {
            // Tạo user mới nếu chưa tồn tại
            User newUser = new User();
            newUser.setUsername(email);
            newUser.setEmail(email);
            newUser.setFullName(name);
            newUser.setPassword(new BCryptPasswordEncoder().encode(UUID.randomUUID().toString())); // OAuth2 users không cần password nhưng cột không được rỗng
            newUser.setEnabled(true);
            
            // Gán role USER mặc định
            Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role USER not found"));
            Set<Role> roles = new HashSet<>();
            roles.add(userRole);
            newUser.setRoles(roles);
            
            appUser = userRepository.save(newUser);
        } else {
            appUser = existingUser.get();
        }

        return new DefaultOAuth2User(
                appUser.getAuthorities().stream().collect(Collectors.toSet()),
                oauth2User.getAttributes(),
                "email"
        );
    }
}
