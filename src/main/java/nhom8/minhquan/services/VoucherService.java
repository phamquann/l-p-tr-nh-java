package nhom8.minhquan.services;

import nhom8.minhquan.entities.Voucher;
import nhom8.minhquan.repositories.IVoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class VoucherService {
    
    private final IVoucherRepository voucherRepository;
    
    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }
    
    public List<Voucher> getActiveVouchers() {
        return voucherRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }
    
    public List<Voucher> getValidVouchers() {
        return voucherRepository.findValidVouchers(LocalDateTime.now());
    }
    
    public Optional<Voucher> getVoucherById(Long id) {
        return voucherRepository.findById(id);
    }
    
    public Optional<Voucher> getVoucherByCode(String code) {
        return voucherRepository.findByCodeIgnoreCase(code);
    }
    
    public Voucher saveVoucher(Voucher voucher) {
        return voucherRepository.save(voucher);
    }
    
    public void deleteVoucher(Long id) {
        voucherRepository.deleteById(id);
    }
    
    public boolean isCodeExists(String code) {
        return voucherRepository.existsByCodeIgnoreCase(code);
    }
    
    public List<Voucher> searchVouchers(String keyword) {
        return voucherRepository.searchVouchers(keyword);
    }
    
    public Optional<Voucher> validateAndApplyVoucher(String code, double orderAmount) {
        Optional<Voucher> voucherOpt = getVoucherByCode(code);
        if (voucherOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Voucher voucher = voucherOpt.get();
        
        if (!voucher.isValid()) {
            return Optional.empty();
        }
        
        if (voucher.getMinOrderAmount() != null && orderAmount < voucher.getMinOrderAmount()) {
            return Optional.empty();
        }
        
        return voucherOpt;
    }
    
    public void incrementUsageCount(Long voucherId) {
        Optional<Voucher> voucherOpt = getVoucherById(voucherId);
        if (voucherOpt.isPresent()) {
            Voucher voucher = voucherOpt.get();
            voucher.setUsedCount(voucher.getUsedCount() + 1);
            saveVoucher(voucher);
        }
    }
    
    public long countVouchers() {
        return voucherRepository.count();
    }
    
    public long countActiveVouchers() {
        return voucherRepository.findByIsActiveTrueOrderByCreatedAtDesc().size();
    }
    
    public long countValidVouchers() {
        return voucherRepository.findValidVouchers(LocalDateTime.now()).size();
    }
}