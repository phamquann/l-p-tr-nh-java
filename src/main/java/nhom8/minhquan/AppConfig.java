package nhom8.minhquan;

import lombok.RequiredArgsConstructor;
import nhom8.minhquan.security.CustomAuthenticationSuccessHandler;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import nhom8.minhquan.entities.Book;
import nhom8.minhquan.entities.Category;
import nhom8.minhquan.entities.Role;
import nhom8.minhquan.entities.User;
import nhom8.minhquan.entities.Voucher;
import nhom8.minhquan.repositories.IBookRepository;
import nhom8.minhquan.repositories.ICategoryRepository;
import nhom8.minhquan.repositories.IRoleRepository;
import nhom8.minhquan.repositories.IUserRepository;
import nhom8.minhquan.repositories.IVoucherRepository;
import nhom8.minhquan.services.CustomOAuth2UserService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class AppConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomAuthenticationSuccessHandler authenticationSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**") // Tắt CSRF cho REST API
            )
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - Static resources
                .requestMatchers("/", "/home", "/index", "/css/**", "/js/**", "/cs/**", "/images/**", "/static/**").permitAll()
                .requestMatchers("/login", "/register", "/access-denied", "/error").permitAll()
                
                // Admin panel
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // REST API endpoints - cho phép HTTP Basic Auth
                .requestMatchers("/api/**").authenticated()
                
                // Web MVC endpoints - books
                .requestMatchers("/books/add", "/books/edit/**", "/books/delete/**").hasRole("ADMIN")
                .requestMatchers("/books", "/books/**").authenticated()
                
                // Cart & Profile
                .requestMatchers("/cart/**", "/profile/**").authenticated()
                
                // Các request còn lại
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                // API sử dụng stateless, Web MVC sử dụng session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(authenticationSuccessHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .successHandler(authenticationSuccessHandler)
                .failureUrl("/login?error=true")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .deleteCookies("remember-me")
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("uniqueAndSecretKeyForRememberMe")
                .tokenValiditySeconds(30 * 24 * 60 * 60) // 30 ngày
                .rememberMeParameter("remember-me")
                .rememberMeCookieName("remember-me")
            )
            .exceptionHandling(exception -> exception
                .accessDeniedPage("/access-denied")
            )
            .httpBasic(Customizer.withDefaults()); // Cho phép HTTP Basic Auth cho API
        
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*")); // Cho phép tất cả origins
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(false);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    public CommandLineRunner initDatabase(
            IBookRepository bookRepository, 
            ICategoryRepository categoryRepository,
            IRoleRepository roleRepository,
            IUserRepository userRepository,
            IVoucherRepository voucherRepository,
            PasswordEncoder passwordEncoder) {
        
        return args -> {
            // Tạo roles nếu chưa có
            if (roleRepository.count() == 0) {
                Role adminRole = new Role();
                adminRole.setName("ROLE_ADMIN");
                adminRole.setDescription("Quản trị viên");
                roleRepository.save(adminRole);

                Role userRole = new Role();
                userRole.setName("ROLE_USER");
                userRole.setDescription("Người dùng");
                roleRepository.save(userRole);

                System.out.println("✅ Đã khởi tạo " + roleRepository.count() + " roles");
            }

            // Tạo users mẫu nếu chưa có
            if (userRepository.count() == 0) {
                Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
                Role userRole = roleRepository.findByName("ROLE_USER").orElseThrow();

                // Tạo admin
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEmail("admin@hutech.edu.vn");
                admin.setFullName("Quản trị viên");
                admin.setPhone("0123456789");
                admin.setEnabled(true);
                Set<Role> adminRoles = new HashSet<>();
                adminRoles.add(adminRole);
                admin.setRoles(adminRoles);
                userRepository.save(admin);

                // Tạo user thường
                User normalUser = new User();
                normalUser.setUsername("user");
                normalUser.setPassword(passwordEncoder.encode("user123"));
                normalUser.setEmail("user@hutech.edu.vn");
                normalUser.setFullName("Người dùng");
                normalUser.setPhone("0987654321");
                normalUser.setEnabled(true);
                Set<Role> userRoles = new HashSet<>();
                userRoles.add(userRole);
                normalUser.setRoles(userRoles);
                userRepository.save(normalUser);

                System.out.println("✅ Đã khởi tạo " + userRepository.count() + " users");
                System.out.println("   👤 Admin - username: admin, password: admin123");
                System.out.println("   👤 User  - username: user, password: user123");
            }

            // Tạo categories nếu chưa có
            if (categoryRepository.count() == 0) {
                Category congNghe = new Category();
                congNghe.setName("Công nghệ thông tin");
                categoryRepository.save(congNghe);

                Category vanHoc = new Category();
                vanHoc.setName("Văn học");
                categoryRepository.save(vanHoc);

                Category khoaHoc = new Category();
                khoaHoc.setName("Khoa học");
                categoryRepository.save(khoaHoc);

                Category kinhTe = new Category();
                kinhTe.setName("Kinh tế");
                categoryRepository.save(kinhTe);

                System.out.println("✅ Đã khởi tạo " + categoryRepository.count() + " thể loại mẫu");
            }

            // Thêm sách mẫu nếu chưa có
            if (bookRepository.count() == 0) {
                Category congNghe = categoryRepository.findAll().get(0);

                Book book1 = new Book();
                book1.setTitle("Lập trình Web Spring Framework");
                book1.setAuthor("Ánh Nguyễn");
                book1.setPrice(29.99);
                book1.setCategory(congNghe);
                bookRepository.save(book1);

                Book book2 = new Book();
                book2.setTitle("Lập trình ứng dụng Java");
                book2.setAuthor("Huy Cường");
                book2.setPrice(45.63);
                book2.setCategory(congNghe);
                bookRepository.save(book2);

                Book book3 = new Book();
                book3.setTitle("Lập trình Web Spring Boot");
                book3.setAuthor("Xuân Nhân");
                book3.setPrice(12.0);
                book3.setCategory(congNghe);
                bookRepository.save(book3);

                Book book4 = new Book();
                book4.setTitle("Lập trình Web Spring MVC");
                book4.setAuthor("Ánh Nguyễn");
                book4.setPrice(0.12);
                book4.setCategory(congNghe);
                bookRepository.save(book4);

                System.out.println("✅ Đã khởi tạo " + bookRepository.count() + " sách mẫu");
            }

            // Tạo vouchers mẫu nếu chưa có
            if (voucherRepository.count() == 0) {
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                java.time.LocalDateTime nextMonth = now.plusMonths(1);

                Voucher voucher1 = new Voucher();
                voucher1.setCode("WELCOME10");
                voucher1.setDescription("Voucher chào mừng giảm 10%");
                voucher1.setDiscountType(Voucher.DiscountType.PERCENTAGE);
                voucher1.setDiscountValue(10.0);
                voucher1.setMinOrderAmount(50000.0);
                voucher1.setUsageLimit(100);
                voucher1.setUsedCount(0);
                voucher1.setStartDate(now);
                voucher1.setEndDate(nextMonth);
                voucher1.setIsActive(true);
                voucherRepository.save(voucher1);

                Voucher voucher2 = new Voucher();
                voucher2.setCode("FIXED50K");
                voucher2.setDescription("Giảm cố định 50.000đ cho đơn hàng từ 500.000đ");
                voucher2.setDiscountType(Voucher.DiscountType.FIXED_AMOUNT);
                voucher2.setDiscountValue(50000.0);
                voucher2.setMinOrderAmount(500000.0);
                voucher2.setUsageLimit(50);
                voucher2.setUsedCount(0);
                voucher2.setStartDate(now);
                voucher2.setEndDate(nextMonth);
                voucher2.setIsActive(true);
                voucherRepository.save(voucher2);

                Voucher voucher3 = new Voucher();
                voucher3.setCode("SUMMER25");
                voucher3.setDescription("Voucher mùa hè giảm 25%");
                voucher3.setDiscountType(Voucher.DiscountType.PERCENTAGE);
                voucher3.setDiscountValue(25.0);
                voucher3.setMinOrderAmount(100000.0);
                voucher3.setUsageLimit(null); // Không giới hạn
                voucher3.setUsedCount(0);
                voucher3.setStartDate(now);
                voucher3.setEndDate(nextMonth);
                voucher3.setIsActive(true);
                voucherRepository.save(voucher3);

                System.out.println("✅ Đã khởi tạo " + voucherRepository.count() + " vouchers mẫu");
            }
        };
    }
}
