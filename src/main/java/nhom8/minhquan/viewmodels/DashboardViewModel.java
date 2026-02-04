package nhom8.minhquan.viewmodels;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ViewModel cho Dashboard/Homepage
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardViewModel {
    // Statistics
    private Long totalBooks;
    private Long totalCategories;
    private Long totalUsers;
    
    // Recent data
    private List<BookViewModel> recentBooks;
    private List<CategoryViewModel> popularCategories;
    
    // User info
    private String currentUser;
    private List<String> userRoles;
}
