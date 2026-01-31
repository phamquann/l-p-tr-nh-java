package nhom8.minhquan;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import nhom8.minhquan.entities.Book;
import nhom8.minhquan.repositories.BookRepository;

@Configuration
public class AppConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/books", "/books/**", "/cs/**", "/js/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(Customizer.withDefaults())
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public CommandLineRunner initDatabase(BookRepository bookRepository) {
        return args -> {
            // Chỉ thêm dữ liệu mẫu nếu database rỗng
            if (bookRepository.count() == 0) {
                bookRepository.save(new Book(
                        null,
                        "Lập trình Web Spring Framework",
                        "Ánh Nguyễn",
                        29.99,
                        "Công nghệ thông tin"
                ));

                bookRepository.save(new Book(
                        null,
                        "Lập trình ứng dụng Java",
                        "Huy Cường",
                        45.63,
                        "Công nghệ thông tin"
                ));

                bookRepository.save(new Book(
                        null,
                        "Lập trình Web Spring Boot",
                        "Xuân Nhân",
                        12.0,
                        "Công nghệ thông tin"
                ));

                bookRepository.save(new Book(
                        null,
                        "Lập trình Web Spring MVC",
                        "Ánh Nguyễn",
                        20.12,
                        "Công nghệ thông tin"
                ));

                System.out.println("✅ Đã khởi tạo " + bookRepository.count() + " sách mẫu vào database");
            }
        };
    }
}
