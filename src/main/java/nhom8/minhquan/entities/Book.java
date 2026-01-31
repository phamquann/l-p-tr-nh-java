package nhom8.minhquan.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "books")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Book {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Tên sách không được để trống")
    @Size(min = 2, max = 200, message = "Tên sách phải từ 2-200 ký tự")
    @Column(nullable = false, length = 200)
    private String title;
    
    @NotBlank(message = "Tác giả không được để trống")
    @Size(min = 2, max = 100, message = "Tên tác giả phải từ 2-100 ký tự")
    @Column(nullable = false, length = 100)
    private String author;
    
    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    @Column(nullable = false)
    private Double price;
    
    @NotBlank(message = "Thể loại không được để trống")
    @Size(min = 2, max = 50, message = "Thể loại phải từ 2-50 ký tự")
    @Column(nullable = false, length = 50)
    private String category;
}