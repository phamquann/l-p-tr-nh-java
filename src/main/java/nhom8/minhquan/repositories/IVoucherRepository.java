package nhom8.minhquan.repositories;

import nhom8.minhquan.entities.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IVoucherRepository extends JpaRepository<Voucher, Long> {
    
    Optional<Voucher> findByCodeIgnoreCase(String code);
    
    boolean existsByCodeIgnoreCase(String code);
    
    List<Voucher> findByIsActiveTrueOrderByCreatedAtDesc();
    
    @Query("SELECT v FROM Voucher v WHERE v.isActive = true AND v.startDate <= :now AND v.endDate >= :now AND (v.usageLimit IS NULL OR v.usedCount < v.usageLimit)")
    List<Voucher> findValidVouchers(@Param("now") LocalDateTime now);
    
    @Query("SELECT v FROM Voucher v WHERE LOWER(v.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(v.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Voucher> searchVouchers(@Param("keyword") String keyword);
}