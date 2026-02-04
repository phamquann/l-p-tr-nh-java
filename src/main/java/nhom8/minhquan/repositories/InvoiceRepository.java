package nhom8.minhquan.repositories;

import nhom8.minhquan.entities.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findAllByOrderByCreatedAtDesc();
    
    List<Invoice> findByStatus(String status);
    
    List<Invoice> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT i FROM Invoice i WHERE i.customerEmail = ?1 ORDER BY i.createdAt DESC")
    List<Invoice> findByCustomerEmail(String email);
    
    @Query("SELECT i FROM Invoice i WHERE LOWER(i.customerName) LIKE LOWER(CONCAT('%', ?1, '%'))")
    List<Invoice> findByCustomerNameContaining(String name);
    
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.createdAt >= ?1")
    Long countByCreatedAtAfter(LocalDateTime date);
    
    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.status = 'COMPLETED'")
    Double getTotalRevenue();
    
    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.status = 'COMPLETED' AND i.createdAt BETWEEN ?1 AND ?2")
    Double getRevenueByDateRange(LocalDateTime start, LocalDateTime end);
}