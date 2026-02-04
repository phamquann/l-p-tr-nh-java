package nhom8.minhquan.models;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model cho Category API requests/responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryModel {
    private Long id;
    
    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;
    
    private Integer bookCount;
}
