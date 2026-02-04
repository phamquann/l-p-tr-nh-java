package nhom8.minhquan.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invoices")
public class Invoice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_name", length = 100)
    private String customerName;
    
    @Column(name = "customer_email", length = 100)
    private String customerEmail;
    
    @Column(name = "customer_phone", length = 20)
    private String customerPhone;
    
    @Column(name = "customer_address", length = 255)
    private String customerAddress;

    @Column(name = "voucher_code", length = 50)
    private String voucherCode;

    @Column(name = "discount_amount")
    private Double discountAmount;
    
    // DB schema requires a non-null `total` column. Keep `total_amount` synced for backward compatibility.
    @Column(name = "total")
    private Double totalAmount;

    @Column(name = "total_amount")
    private Double totalAmountLegacy;
    
    @Column(name = "status", length = 50)
    private String status; // PENDING, CONFIRMED, SHIPPING, COMPLETED, CANCELLED
    
    @Column(name = "invoice_date", nullable = false)
    private LocalDateTime invoiceDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemInvoice> items = new ArrayList<>();
    
    // Custom builder method để đảm bảo items được khởi tạo
    public static InvoiceBuilder builder() {
        return new InvoiceBuilder();
    }
    
    public static class InvoiceBuilder {
        private String customerName;
        private String customerEmail;
        private String customerPhone;
        private String customerAddress;
        private Double totalAmount;
        private String status;
        
        public InvoiceBuilder customerName(String customerName) {
            this.customerName = customerName;
            return this;
        }
        
        public InvoiceBuilder customerEmail(String customerEmail) {
            this.customerEmail = customerEmail;
            return this;
        }
        
        public InvoiceBuilder customerPhone(String customerPhone) {
            this.customerPhone = customerPhone;
            return this;
        }
        
        public InvoiceBuilder customerAddress(String customerAddress) {
            this.customerAddress = customerAddress;
            return this;
        }
        
        public InvoiceBuilder totalAmount(Double totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }
        
        public InvoiceBuilder status(String status) {
            this.status = status;
            return this;
        }
        
        public Invoice build() {
            Invoice invoice = new Invoice();
            invoice.setCustomerName(this.customerName);
            invoice.setCustomerEmail(this.customerEmail);
            invoice.setCustomerPhone(this.customerPhone);
            invoice.setCustomerAddress(this.customerAddress);
            invoice.setTotalAmount(this.totalAmount);
            invoice.setStatus(this.status);
            invoice.setItems(new ArrayList<>()); // Đảm bảo items được khởi tạo
            return invoice;
        }
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        invoiceDate = LocalDateTime.now();
        if (totalAmountLegacy == null) {
            totalAmountLegacy = totalAmount;
        }
        if (status == null) {
            status = "PENDING";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (totalAmountLegacy == null) {
            totalAmountLegacy = totalAmount;
        }
    }
    
    public void addItem(ItemInvoice item) {
        items.add(item);
        item.setInvoice(this);
    }
    
    public void removeItem(ItemInvoice item) {
        items.remove(item);
        item.setInvoice(null);
    }
}
