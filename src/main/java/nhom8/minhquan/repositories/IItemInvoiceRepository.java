package nhom8.minhquan.repositories;

import nhom8.minhquan.entities.ItemInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IItemInvoiceRepository extends JpaRepository<ItemInvoice, Long> {
    
    // Tìm tất cả item theo invoice ID
    List<ItemInvoice> findByInvoiceId(Long invoiceId);
    
    // Tìm tất cả item theo book ID
    List<ItemInvoice> findByBookId(Long bookId);
    
    // Đếm số lượng item trong invoice
    Long countByInvoiceId(Long invoiceId);
    
    // Tính tổng tiền của invoice
    @Query("SELECT SUM(i.subtotal) FROM ItemInvoice i WHERE i.invoice.id = :invoiceId")
    Double sumSubtotalByInvoiceId(Long invoiceId);
    
    // Lấy top sách bán chạy
    @Query("SELECT i.book.id, i.book.title, SUM(i.quantity) as totalSold " +
           "FROM ItemInvoice i " +
           "GROUP BY i.book.id, i.book.title " +
           "ORDER BY totalSold DESC")
    List<Object[]> findBestSellingBooks();
}
