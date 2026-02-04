package nhom8.minhquan.repositories;

import nhom8.minhquan.entities.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IInvoiceRepository extends JpaRepository<Invoice, Long> {
    
    // Tìm hóa đơn theo trạng thái
    List<Invoice> findByStatus(String status);
    
    // Tìm hóa đơn theo email khách hàng
    List<Invoice> findByCustomerEmail(String email);
    
    // Tìm hóa đơn theo email, sắp xếp theo ngày tạo mới nhất
    List<Invoice> findByCustomerEmailOrderByCreatedAtDesc(String email);
    
    // Tìm hóa đơn theo số điện thoại
    List<Invoice> findByCustomerPhone(String phone);
    
    // Tìm hóa đơn trong khoảng thời gian
    @Query("SELECT i FROM Invoice i WHERE i.createdAt BETWEEN :startDate AND :endDate ORDER BY i.createdAt DESC")
    List<Invoice> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Tìm hóa đơn mới nhất
    List<Invoice> findTop10ByOrderByCreatedAtDesc();
    
    // Tìm tất cả hóa đơn, sắp xếp theo ngày tạo mới nhất
    List<Invoice> findAllByOrderByCreatedAtDesc();
    
    // Đếm số hóa đơn theo trạng thái
    Long countByStatus(String status);
}
