package nhom8.minhquan.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated Response Model
 * Sử dụng cho các API trả về danh sách có phân trang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponseModel<T> {
    private List<T> content;
    private int page;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean empty;
    
    public static <T> PagedResponseModel<T> of(List<T> content, int page, int pageSize, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        
        return PagedResponseModel.<T>builder()
                .content(content)
                .page(page)
                .pageSize(pageSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .empty(content.isEmpty())
                .build();
    }
}
