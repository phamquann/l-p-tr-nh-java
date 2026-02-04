package nhom8.minhquan.controllers.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nhom8.minhquan.models.*;
import nhom8.minhquan.entities.Book;
import nhom8.minhquan.entities.Category;
import nhom8.minhquan.services.BookService;
import nhom8.minhquan.services.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * API Controller cho Books
 * Endpoint: /api/v1/books
 * Sử dụng Models thay vì DTOs
 */
@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookApiController {
    
    private final BookService bookService;
    private final CategoryService categoryService;
    
    /**
     * GET /api/v1/books - Lấy danh sách sách (có phân trang)
     */
    @GetMapping
    public ResponseEntity<ApiResponseModel<PagedResponseModel<BookModel>>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        long startTime = System.currentTimeMillis();
        
        // Create pageable
        Sort sort = sortDir.equalsIgnoreCase("ASC") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Get all books (simplified - thực tế nên dùng repository.findAll(pageable))
        List<BookModel> allBooks = bookService.getAllBooks().stream()
                .map(this::convertToModel)
                .collect(Collectors.toList());
        
        // Manual pagination
        int start = page * size;
        int end = Math.min(start + size, allBooks.size());
        List<BookModel> pagedBooks = allBooks.subList(start, end);
        
        PagedResponseModel<BookModel> pagedResponse = PagedResponseModel.of(
                pagedBooks, page, size, allBooks.size()
        );
        
        // Create metadata
        ApiMetadata metadata = ApiMetadata.builder()
                .page(page)
                .pageSize(size)
                .totalElements((long) allBooks.size())
                .totalPages((int) Math.ceil((double) allBooks.size() / size))
                .sortBy(sortBy)
                .sortDirection(sortDir)
                .requestId(UUID.randomUUID().toString())
                .executionTime(System.currentTimeMillis() - startTime)
                .build();
        
        ApiResponseModel<PagedResponseModel<BookModel>> response = ApiResponseModel.<PagedResponseModel<BookModel>>builder()
                .success(true)
                .message("Lấy danh sách sách thành công")
                .data(pagedResponse)
                .timestamp(LocalDateTime.now())
                .metadata(metadata)
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/v1/books/{id} - Lấy chi tiết 1 sách
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseModel<BookModel>> getBookById(@PathVariable Long id) {
        long startTime = System.currentTimeMillis();
        
        Book book = bookService.getBookById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách với ID: " + id));
        
        ApiMetadata metadata = ApiMetadata.builder()
                .requestId(UUID.randomUUID().toString())
                .executionTime(System.currentTimeMillis() - startTime)
                .build();
        
        ApiResponseModel<BookModel> response = ApiResponseModel.<BookModel>builder()
                .success(true)
                .message("Lấy thông tin sách thành công")
                .data(convertToModel(book))
                .timestamp(LocalDateTime.now())
                .metadata(metadata)
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/v1/books - Tạo sách mới (ADMIN only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseModel<BookModel>> createBook(@Valid @RequestBody BookModel bookModel) {
        long startTime = System.currentTimeMillis();
        
        Book book = convertToEntity(bookModel);
        bookService.saveBook(book);
        
        ApiMetadata metadata = ApiMetadata.builder()
                .requestId(UUID.randomUUID().toString())
                .executionTime(System.currentTimeMillis() - startTime)
                .build();
        
        ApiResponseModel<BookModel> response = ApiResponseModel.<BookModel>builder()
                .success(true)
                .message("Tạo sách mới thành công")
                .data(convertToModel(book))
                .timestamp(LocalDateTime.now())
                .statusCode(201)
                .metadata(metadata)
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * PUT /api/v1/books/{id} - Cập nhật sách (ADMIN only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseModel<BookModel>> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookModel bookModel) {
        
        long startTime = System.currentTimeMillis();
        
        Book book = convertToEntity(bookModel);
        bookService.updateBook(id, book);
        
        Book updatedBook = bookService.getBookById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách với ID: " + id));
        
        ApiMetadata metadata = ApiMetadata.builder()
                .requestId(UUID.randomUUID().toString())
                .executionTime(System.currentTimeMillis() - startTime)
                .build();
        
        ApiResponseModel<BookModel> response = ApiResponseModel.<BookModel>builder()
                .success(true)
                .message("Cập nhật sách thành công")
                .data(convertToModel(updatedBook))
                .timestamp(LocalDateTime.now())
                .metadata(metadata)
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * DELETE /api/v1/books/{id} - Xóa sách (ADMIN only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseModel<Void>> deleteBook(@PathVariable Long id) {
        long startTime = System.currentTimeMillis();
        
        bookService.deleteBook(id);
        
        ApiMetadata metadata = ApiMetadata.builder()
                .requestId(UUID.randomUUID().toString())
                .executionTime(System.currentTimeMillis() - startTime)
                .build();
        
        ApiResponseModel<Void> response = ApiResponseModel.<Void>builder()
                .success(true)
                .message("Xóa sách thành công")
                .timestamp(LocalDateTime.now())
                .metadata(metadata)
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/v1/books/search - Tìm kiếm sách theo tiêu đề hoặc tác giả
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponseModel<List<BookModel>>> searchBooks(
            @RequestParam String keyword) {
        
        long startTime = System.currentTimeMillis();
        
        List<BookModel> books = bookService.getAllBooks().stream()
                .filter(book -> 
                    book.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                    book.getAuthor().toLowerCase().contains(keyword.toLowerCase())
                )
                .map(this::convertToModel)
                .collect(Collectors.toList());
        
        ApiMetadata metadata = ApiMetadata.builder()
                .filter("keyword=" + keyword)
                .requestId(UUID.randomUUID().toString())
                .executionTime(System.currentTimeMillis() - startTime)
                .build();
        
        ApiResponseModel<List<BookModel>> response = ApiResponseModel.<List<BookModel>>builder()
                .success(true)
                .message("Tìm thấy " + books.size() + " kết quả")
                .data(books)
                .timestamp(LocalDateTime.now())
                .metadata(metadata)
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/v1/books/stats - Thống kê sách
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponseModel<BookStatsModel>> getBookStats() {
        long totalBooks = bookService.countBooks();
        double avgPrice = bookService.getAllBooks().stream()
                .mapToDouble(Book::getPrice)
                .average()
                .orElse(0.0);
        
        BookStatsModel stats = BookStatsModel.builder()
                .totalBooks(totalBooks)
                .averagePrice(avgPrice)
                .build();
        
        return ResponseEntity.ok(ApiResponseModel.success("Thống kê sách", stats));
    }
    
    // Helper methods
    private BookModel convertToModel(Book book) {
        return BookModel.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .price(book.getPrice())
                .categoryId(book.getCategory() != null ? book.getCategory().getId() : null)
                .categoryName(book.getCategory() != null ? book.getCategory().getName() : null)
                .build();
    }
    
    private Book convertToEntity(BookModel model) {
        Category category = null;
        if (model.getCategoryId() != null) {
            category = categoryService.getCategoryById(model.getCategoryId())
                    .orElse(null);
        }
        
        return Book.builder()
                .title(model.getTitle())
                .author(model.getAuthor())
                .price(model.getPrice())
                .category(category)
                .build();
    }
    
    // Nested Stats Model
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BookStatsModel {
        private Long totalBooks;
        private Double averagePrice;
    }
}
