package nhom8.minhquan.controllers;

import nhom8.minhquan.entities.Voucher;
import nhom8.minhquan.services.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/vouchers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class VoucherController {
    
    private final VoucherService voucherService;
    
    @GetMapping
    public String getAllVouchers(@RequestParam(value = "search", required = false) String keyword,
                                @RequestParam(value = "status", required = false) String status,
                                Model model) {
        List<Voucher> vouchers;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            vouchers = voucherService.searchVouchers(keyword);
            model.addAttribute("search", keyword);
        } else {
            vouchers = voucherService.getAllVouchers();
        }
        
        // Filter by status if provided
        if (status != null && !status.trim().isEmpty()) {
            vouchers = vouchers.stream()
                    .filter(v -> {
                        if ("ACTIVE".equals(status)) {
                            return v.getIsActive() && v.isValid();
                        } else if ("INACTIVE".equals(status)) {
                            return !v.getIsActive();
                        } else if ("EXPIRED".equals(status)) {
                            return v.isExpired();
                        }
                        return true;
                    })
                    .toList();
            model.addAttribute("status", status);
        }
        
        model.addAttribute("vouchers", vouchers);
        model.addAttribute("totalVouchers", voucherService.countVouchers());
        model.addAttribute("activeVouchers", voucherService.countActiveVouchers());
        model.addAttribute("validVouchers", voucherService.countValidVouchers());
        
        // Calculate expiring vouchers (within 7 days)
        LocalDateTime sevenDaysLater = LocalDateTime.now().plusDays(7);
        long expiringVouchers = voucherService.getAllVouchers().stream()
                .filter(v -> v.isValid() && v.getEndDate().isBefore(sevenDaysLater))
                .count();
        model.addAttribute("expiringVouchers", expiringVouchers);
        
        // Calculate expired vouchers
        long expiredVouchers = voucherService.getAllVouchers().stream()
                .filter(Voucher::isExpired)
                .count();
        model.addAttribute("expiredVouchers", expiredVouchers);
        
        return "admin/vouchers";
    }
    
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("voucher", new Voucher());
        model.addAttribute("isEdit", false);
        model.addAttribute("discountTypes", Voucher.DiscountType.values());
        return "admin/voucher-form";
    }
    
    @PostMapping("/add")
    public String addVoucher(@Valid @ModelAttribute Voucher voucher, 
                           BindingResult bindingResult, 
                           RedirectAttributes redirectAttributes, 
                           Model model) {
        
        if (voucherService.isCodeExists(voucher.getCode())) {
            bindingResult.rejectValue("code", "error.voucher", "Mã voucher đã tồn tại");
        }
        
        if (voucher.getEndDate().isBefore(voucher.getStartDate())) {
            bindingResult.rejectValue("endDate", "error.voucher", "Ngày kết thúc phải sau ngày bắt đầu");
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            model.addAttribute("discountTypes", Voucher.DiscountType.values());
            return "admin/voucher-form";
        }
        
        try {
            voucher.setUsedCount(0);
            voucherService.saveVoucher(voucher);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo voucher thành công!");
            return "redirect:/admin/vouchers";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/admin/vouchers/add";
        }
    }
    
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Voucher voucher = voucherService.getVoucherById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher với ID: " + id));
            
            model.addAttribute("voucher", voucher);
            model.addAttribute("isEdit", true);
            model.addAttribute("discountTypes", Voucher.DiscountType.values());
            return "admin/voucher-form";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy voucher: " + e.getMessage());
            return "redirect:/admin/vouchers";
        }
    }
    
    @PostMapping("/edit/{id}")
    public String updateVoucher(@PathVariable Long id,
                              @Valid @ModelAttribute Voucher voucher,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        
        try {
            Voucher existingVoucher = voucherService.getVoucherById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher"));
            
            if (voucher.getEndDate().isBefore(voucher.getStartDate())) {
                bindingResult.rejectValue("endDate", "error.voucher", "Ngày kết thúc phải sau ngày bắt đầu");
            }
            
            if (bindingResult.hasErrors()) {
                model.addAttribute("isEdit", true);
                model.addAttribute("discountTypes", Voucher.DiscountType.values());
                return "admin/voucher-form";
            }
            
            // Giữ nguyên các giá trị quan trọng
            voucher.setId(id);
            voucher.setCode(existingVoucher.getCode()); // Không cho phép thay đổi mã
            voucher.setUsedCount(existingVoucher.getUsedCount());
            voucher.setCreatedAt(existingVoucher.getCreatedAt());
            
            voucherService.saveVoucher(voucher);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật voucher thành công!");
            return "redirect:/admin/vouchers";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/admin/vouchers/edit/" + id;
        }
    }
    
    @PostMapping("/toggle-status/{id}")
    public String toggleVoucherStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Voucher voucher = voucherService.getVoucherById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher"));
            
            voucher.setIsActive(!voucher.getIsActive());
            voucherService.saveVoucher(voucher);
            
            String status = voucher.getIsActive() ? "kích hoạt" : "tạm dừng";
            redirectAttributes.addFlashAttribute("successMessage", "Đã " + status + " voucher thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return "redirect:/admin/vouchers";
    }
    
    @PostMapping("/delete/{id}")
    public String deleteVoucher(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Voucher voucher = voucherService.getVoucherById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy voucher"));
            
            if (voucher.getUsedCount() > 0) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Không thể xóa voucher đã được sử dụng " + voucher.getUsedCount() + " lần!");
                return "redirect:/admin/vouchers";
            }
            
            voucherService.deleteVoucher(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa voucher thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return "redirect:/admin/vouchers";
    }
}