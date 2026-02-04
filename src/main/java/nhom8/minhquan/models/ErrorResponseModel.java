package nhom8.minhquan.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Error Response Model cho API
 * Sử dụng khi có lỗi xảy ra
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseModel {
    private boolean success;
    private String message;
    private int statusCode;
    private String errorCode;
    private String errorType;
    private List<ValidationError> validationErrors;
    private String path;
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    // Nested class for validation errors
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
        private Object rejectedValue;
    }
    
    // Static factory methods
    public static ErrorResponseModel badRequest(String message) {
        return ErrorResponseModel.builder()
                .success(false)
                .message(message)
                .statusCode(400)
                .errorType("Bad Request")
                .build();
    }
    
    public static ErrorResponseModel unauthorized(String message) {
        return ErrorResponseModel.builder()
                .success(false)
                .message(message)
                .statusCode(401)
                .errorType("Unauthorized")
                .build();
    }
    
    public static ErrorResponseModel forbidden(String message) {
        return ErrorResponseModel.builder()
                .success(false)
                .message(message)
                .statusCode(403)
                .errorType("Forbidden")
                .build();
    }
    
    public static ErrorResponseModel notFound(String message) {
        return ErrorResponseModel.builder()
                .success(false)
                .message(message)
                .statusCode(404)
                .errorType("Not Found")
                .build();
    }
    
    public static ErrorResponseModel internalServerError(String message) {
        return ErrorResponseModel.builder()
                .success(false)
                .message(message)
                .statusCode(500)
                .errorType("Internal Server Error")
                .build();
    }
}
