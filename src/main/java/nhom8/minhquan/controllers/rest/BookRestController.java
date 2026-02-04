package nhom8.minhquan.controllers.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nhom8.minhquan.dto.ApiResponse;
import nhom8.minhquan.dto.BookDTO;
import nhom8.minhquan.entities.Book;
import nhom8.minhquan.entities.Category;
import nhom8.minhquan.services.BookService;
import nhom8.minhquan.services.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookRestController {
    
    private final BookService bookService;
    private final CategoryService categoryService;
    
    /**
     * GET /api/books - Lấy danh sách tất cả sách
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BookDTO>>> getAllBooks() {
        List<BookDTO> books = bookService.getAllBooks().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(
            ApiResponse.success("Lấy danh sách sách thành công", books)
        );
    }
    
    /**
     * GET /api/books/{id} - Lấy thông tin chi tiết 1 sách
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookDTO>> getBookById(@PathVariable Long id) {
        Book book = bookService.getBookById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách với ID: " + id));
        
        return ResponseEntity.ok(
            ApiResponse.success("Lấy thông tin sách thành công", convertToDTO(book))
        );
    }
    
    /**
     * POST /api/books - Tạo sách mới (Chỉ ADMIN)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookDTO>> createBook(@Valid @RequestBody BookDTO bookDTO) {
        Book book = convertToEntity(bookDTO);
        bookService.saveBook(book);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success("Thêm sách thành công", convertToDTO(book))
        );
    }
    
    /**
     * PUT /api/books/{id} - Cập nhật sách (Chỉ ADMIN)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookDTO>> updateBook(
            @PathVariable Long id, 
            @Valid @RequestBody BookDTO bookDTO) {
        
        Book book = convertToEntity(bookDTO);
        bookService.updateBook(id, book);
        
        Book updatedBook = bookService.getBookById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách với ID: " + id));
        
        return ResponseEntity.ok(
            ApiResponse.success("Cập nhật sách thành công", convertToDTO(updatedBook))
        );
    }
    
    /**
     * DELETE /api/books/{id} - Xóa sách (Chỉ ADMIN)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        
        return ResponseEntity.ok(
            ApiResponse.success("Xóa sách thành công", null)
        );
    }
    
    /**
     * GET /api/books/count - Đếm tổng số sách
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> countBooks() {
        long count = bookService.countBooks();
        
        return ResponseEntity.ok(
            ApiResponse.success("Đếm sách thành công", count)
        );
    }
    
    // Helper methods
    private BookDTO convertToDTO(Book book) {
        return BookDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .price(book.getPrice())
                .categoryId(book.getCategory() != null ? book.getCategory().getId() : null)
                .categoryName(book.getCategory() != null ? book.getCategory().getName() : null)
                .build();
    }
    
    private Book convertToEntity(BookDTO dto) {
        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryService.getCategoryById(dto.getCategoryId())
                    .orElse(null);
        }
        
        return Book.builder()
                .title(dto.getTitle())
                .author(dto.getAuthor())
                .price(dto.getPrice())
                .category(category)
                .build();
    }
}
