package nhom8.minhquan.viewmodels;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ViewModel cho Category - sử dụng trong Web MVC Controllers
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryViewModel {
    private Long id;
    
    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;
    
    // Số lượng sách trong category này
    private Integer bookCount;
    
    // Computed properties
    public String getBookCountText() {
        if (bookCount == null || bookCount == 0) {
            return "Chưa có sách";
        }
        return bookCount + " cuốn sách";
    }
}
