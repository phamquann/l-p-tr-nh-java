package nhom8.minhquan.viewmodels;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ViewModel cho Book - sử dụng trong Web MVC Controllers
 * Dùng để hiển thị và nhận dữ liệu từ forms
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookViewModel {
    private Long id;
    
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;
    
    @NotBlank(message = "Tác giả không được để trống")
    private String author;
    
    @NotNull(message = "Giá không được để trống")
    @Positive(message = "Giá phải lớn hơn 0")
    private Double price;
    
    @NotNull(message = "Vui lòng chọn danh mục")
    private Long categoryId;
    
    // Thông tin category để hiển thị
    private String categoryName;
    
    // Computed properties
    public String getPriceFormatted() {
        if (price == null) return "0đ";
        return String.format("%,.0fđ", price);
    }
    
    public String getDisplayName() {
        return title + " - " + author;
    }
}
