package nhom8.minhquan.controllers.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nhom8.minhquan.models.*;
import nhom8.minhquan.entities.Category;
import nhom8.minhquan.services.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * API Controller cho Categories
 * Endpoint: /api/v1/categories
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryApiController {
    
    private final CategoryService categoryService;
    
    /**
     * GET /api/v1/categories - Lấy tất cả danh mục
     */
    @GetMapping
    public ResponseEntity<ApiResponseModel<List<CategoryModel>>> getAllCategories() {
        long startTime = System.currentTimeMillis();
        
        List<CategoryModel> categories = categoryService.getAllCategories().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
        
        ApiMetadata metadata = ApiMetadata.builder()
                .totalElements((long) categories.size())
                .requestId(UUID.randomUUID().toString())
                .executionTime(System.currentTimeMillis() - startTime)
                .build();
        
        ApiResponseModel<List<CategoryModel>> response = ApiResponseModel.<List<CategoryModel>>builder()
                .success(true)
                .message("Lấy danh sách danh mục thành công")
                .data(categories)
                .timestamp(LocalDateTime.now())
                .metadata(metadata)
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/v1/categories/{id} - Lấy 1 danh mục
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseModel<CategoryModel>> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục với ID: " + id));
        
        return ResponseEntity.ok(
            ApiResponseModel.success("Lấy thông tin danh mục thành công", convertToModel(category))
        );
    }
    
    /**
     * POST /api/v1/categories - Tạo danh mục mới (ADMIN only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseModel<CategoryModel>> createCategory(
            @Valid @RequestBody CategoryModel categoryModel) {
        
        Category category = Category.builder()
                .name(categoryModel.getName())
                .build();
        
        categoryService.saveCategory(category);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponseModel.created("Tạo danh mục thành công", convertToModel(category))
        );
    }
    
    /**
     * PUT /api/v1/categories/{id} - Cập nhật danh mục (ADMIN only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseModel<CategoryModel>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryModel categoryModel) {
        
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục với ID: " + id));
        
        category.setName(categoryModel.getName());
        categoryService.saveCategory(category);
        
        return ResponseEntity.ok(
            ApiResponseModel.success("Cập nhật danh mục thành công", convertToModel(category))
        );
    }
    
    /**
     * DELETE /api/v1/categories/{id} - Xóa danh mục (ADMIN only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseModel<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        
        return ResponseEntity.ok(
            ApiResponseModel.success("Xóa danh mục thành công", null)
        );
    }
    
    // Helper method
    private CategoryModel convertToModel(Category category) {
        return CategoryModel.builder()
                .id(category.getId())
                .name(category.getName())
                .bookCount(category.getBooks() != null ? category.getBooks().size() : 0)
                .build();
    }
}
