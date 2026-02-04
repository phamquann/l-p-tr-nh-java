package nhom8.minhquan.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Metadata cho API responses
 * Chứa thông tin phân trang, sorting, filtering
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiMetadata {
    // Pagination
    private Integer page;
    private Integer pageSize;
    private Long totalElements;
    private Integer totalPages;
    
    // Sorting
    private String sortBy;
    private String sortDirection;
    
    // Filtering
    private String filter;
    
    // Request info
    private String requestId;
    private Long executionTime; // milliseconds
}
