package nhom8.minhquan.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Base Response Model cho tất cả API responses
 * Chuẩn hóa format response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseModel<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private ApiMetadata metadata;
    
    @Builder.Default
    private int statusCode = 200;
    
    // Static factory methods
    public static <T> ApiResponseModel<T> success(T data) {
        return ApiResponseModel.<T>builder()
                .success(true)
                .message("Thao tác thành công")
                .data(data)
                .timestamp(LocalDateTime.now())
                .statusCode(200)
                .build();
    }
    
    public static <T> ApiResponseModel<T> success(String message, T data) {
        return ApiResponseModel.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .statusCode(200)
                .build();
    }
    
    public static <T> ApiResponseModel<T> created(String message, T data) {
        return ApiResponseModel.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .statusCode(201)
                .build();
    }
    
    public static <T> ApiResponseModel<T> error(String message) {
        return ApiResponseModel.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .statusCode(400)
                .build();
    }
    
    public static <T> ApiResponseModel<T> error(int statusCode, String message) {
        return ApiResponseModel.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .statusCode(statusCode)
                .build();
    }
}
