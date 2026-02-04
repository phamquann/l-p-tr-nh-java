package nhom8.minhquan.services;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import nhom8.minhquan.daos.Cart;
import nhom8.minhquan.daos.Item;
import nhom8.minhquan.entities.*;
import nhom8.minhquan.repositories.ICartRepository;
import nhom8.minhquan.repositories.IInvoiceRepository;
import nhom8.minhquan.repositories.IItemInvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
    
    private final IInvoiceRepository invoiceRepository;
    private final IItemInvoiceRepository itemInvoiceRepository;
    private final ICartRepository cartRepository;
    private final BookService bookService;
    
    private static final String CART_SESSION_KEY = "cart";
    
    /**
     * Lấy giỏ hàng từ session
     */
    public Cart getCart(HttpSession session) {
        Cart cart = (Cart) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new Cart();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }
    
    /**
     * Lưu giỏ hàng vào database cho user đã đăng nhập
     */
    public void saveCartToDatabase(User user, Cart cart) {
        if (user == null || cart == null) return;
        
        // Tìm hoặc tạo CartEntity cho user
        CartEntity cartEntity = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    CartEntity newCart = new CartEntity();
                    newCart.setUser(user);
                    return newCart;
                });
        
        // Xóa các items cũ
        cartEntity.clearItems();
        
        // Thêm các items mới
        for (Item item : cart.getItems()) {
            Book book = bookService.getBookById(item.getBookId()).orElse(null);
            if (book != null) {
                CartItemEntity cartItem = new CartItemEntity(cartEntity, book, item.getQuantity());
                cartEntity.addItem(cartItem);
            }
        }
        
        cartRepository.save(cartEntity);
    }
    
    /**
     * Load giỏ hàng từ database vào session khi user đăng nhập
     */
    public void loadCartFromDatabase(User user, HttpSession session) {
        if (user == null) return;
        
        Optional<CartEntity> cartEntityOpt = cartRepository.findByUser(user);
        
        if (cartEntityOpt.isPresent()) {
            CartEntity cartEntity = cartEntityOpt.get();
            Cart sessionCart = getCart(session);
            
            // Merge: giữ lại items trong session và thêm items từ database
            for (CartItemEntity cartItem : cartEntity.getItems()) {
                Book book = cartItem.getBook();
                if (book != null) {
                    // Kiểm tra xem sách đã có trong session cart chưa
                    boolean exists = sessionCart.getItems().stream()
                            .anyMatch(i -> i.getBookId().equals(book.getId()));
                    
                    if (!exists) {
                        // Thêm sách từ database vào session
                        for (int i = 0; i < cartItem.getQuantity(); i++) {
                            sessionCart.addItem(book);
                        }
                        // Điều chỉnh lại số lượng đúng
                        sessionCart.updateQuantity(book.getId(), cartItem.getQuantity());
                    }
                }
            }
            
            session.setAttribute(CART_SESSION_KEY, sessionCart);
        }
    }
    
    /**
     * Xóa giỏ hàng trong database
     */
    public void clearCartInDatabase(User user) {
        if (user == null) return;
        cartRepository.findByUser(user).ifPresent(cartRepository::delete);
    }
    
    /**
     * Thêm sách vào giỏ hàng
     */
    public void addToCart(HttpSession session, Book book, User user) {
        Cart cart = getCart(session);
        cart.addItem(book);
        session.setAttribute(CART_SESSION_KEY, cart);
        
        // Lưu vào database nếu user đã đăng nhập
        if (user != null) {
            saveCartToDatabase(user, cart);
        }
    }
    
    /**
     * Cập nhật số lượng sách trong giỏ
     */
    public void updateQuantity(HttpSession session, Long bookId, int quantity, User user) {
        Cart cart = getCart(session);
        cart.updateQuantity(bookId, quantity);
        session.setAttribute(CART_SESSION_KEY, cart);
        
        // Lưu vào database nếu user đã đăng nhập
        if (user != null) {
            saveCartToDatabase(user, cart);
        }
    }
    
    /**
     * Xóa sách khỏi giỏ hàng
     */
    public void removeFromCart(HttpSession session, Long bookId, User user) {
        Cart cart = getCart(session);
        cart.removeItem(bookId);
        session.setAttribute(CART_SESSION_KEY, cart);
        
        // Lưu vào database nếu user đã đăng nhập
        if (user != null) {
            saveCartToDatabase(user, cart);
        }
    }
    
    /**
     * Xóa toàn bộ giỏ hàng
     */
    public void clearCart(HttpSession session, User user) {
        Cart cart = getCart(session);
        cart.clear();
        session.setAttribute(CART_SESSION_KEY, cart);
        
        // Xóa trong database nếu user đã đăng nhập
        if (user != null) {
            clearCartInDatabase(user);
        }
    }
    
    // Legacy methods for backward compatibility
    public void addToCart(HttpSession session, Book book) {
        addToCart(session, book, null);
    }
    
    public void updateQuantity(HttpSession session, Long bookId, int quantity) {
        updateQuantity(session, bookId, quantity, null);
    }
    
    public void removeFromCart(HttpSession session, Long bookId) {
        removeFromCart(session, bookId, null);
    }
    
    public void clearCart(HttpSession session) {
        clearCart(session, null);
    }
    
    /**
     * Tạo hóa đơn từ giỏ hàng
     */
    public Invoice createInvoiceFromCart(HttpSession session, String customerName, 
                                         String customerEmail, String customerPhone, 
                                         String customerAddress, User user) {
        Cart cart = getCart(session);
        
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Giỏ hàng trống, không thể tạo hóa đơn");
        }
        
        // Tạo invoice (KHÔNG có items)
        Invoice invoice = Invoice.builder()
                .customerName(customerName)
                .customerEmail(customerEmail)
                .customerPhone(customerPhone)
                .customerAddress(customerAddress)
                .totalAmount(cart.getTotalPrice())
                .status("PENDING")
                .build();
        
        // LƯU INVOICE TRƯỚC để có ID
        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        // SAU ĐÓ mới thêm các items vào invoice
        for (Item cartItem : cart.getItems()) {
            Book book = bookService.getBookById(cartItem.getBookId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Không tìm thấy sách với ID: " + cartItem.getBookId()));
            
            ItemInvoice itemInvoice = ItemInvoice.builder()
                    .invoice(savedInvoice)  // Set invoice đã có ID
                    .book(book)
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getPrice())
                    .build();
            
            savedInvoice.getItems().add(itemInvoice);
        }
        
        // Lưu lại invoice với items
        savedInvoice = invoiceRepository.save(savedInvoice);
        
        // Xóa giỏ hàng sau khi tạo hóa đơn (cả session và database)
        clearCart(session, user);
        
        return savedInvoice;
    }

    /**
     * Tạo hóa đơn từ giỏ hàng với voucher (nếu có)
     */
    public Invoice createInvoiceFromCart(HttpSession session, String customerName,
                                         String customerEmail, String customerPhone,
                                         String customerAddress, User user,
                                         String voucherCode, Double discountAmount,
                                         Double finalTotal) {
        Cart cart = getCart(session);

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Giỏ hàng trống, không thể tạo hóa đơn");
        }

        double totalToUse = (finalTotal != null) ? finalTotal : cart.getTotalPrice();

        Invoice invoice = Invoice.builder()
                .customerName(customerName)
                .customerEmail(customerEmail)
                .customerPhone(customerPhone)
                .customerAddress(customerAddress)
                .totalAmount(totalToUse)
                .status("PENDING")
                .build();

        if (voucherCode != null && !voucherCode.isBlank()) {
            invoice.setVoucherCode(voucherCode.trim());
            invoice.setDiscountAmount(discountAmount != null ? discountAmount : 0.0);
        }

        Invoice savedInvoice = invoiceRepository.save(invoice);

        for (Item cartItem : cart.getItems()) {
            Book book = bookService.getBookById(cartItem.getBookId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Không tìm thấy sách với ID: " + cartItem.getBookId()));

            ItemInvoice itemInvoice = ItemInvoice.builder()
                    .invoice(savedInvoice)
                    .book(book)
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getPrice())
                    .build();

            savedInvoice.getItems().add(itemInvoice);
        }

        savedInvoice = invoiceRepository.save(savedInvoice);

        clearCart(session, user);

        return savedInvoice;
    }
    
    // Backward compatible method
    public Invoice createInvoiceFromCart(HttpSession session, String customerName, 
                                         String customerEmail, String customerPhone, 
                                         String customerAddress) {
        return createInvoiceFromCart(session, customerName, customerEmail, customerPhone, customerAddress, null);
    }
    
    /**
     * Lấy tất cả hóa đơn
     */
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Lấy hóa đơn theo ID
     */
    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hóa đơn với ID: " + id));
    }

    /**
     * Lấy hóa đơn theo trạng thái
     */
    public List<Invoice> getInvoicesByStatus(String status) {
        return invoiceRepository.findByStatus(status);
    }
    
    /**
     * Lấy hóa đơn theo email khách hàng
     */
    public List<Invoice> getInvoicesByCustomerEmail(String email) {
        return invoiceRepository.findByCustomerEmailOrderByCreatedAtDesc(email);
    }
    
    /**
     * Cập nhật trạng thái hóa đơn
     */
    public void updateInvoiceStatus(Long invoiceId, String status) {
        Invoice invoice = getInvoiceById(invoiceId);
        invoice.setStatus(status);
        invoiceRepository.save(invoice);
    }
    
    /**
     * Hủy hóa đơn
     */
    public void cancelInvoice(Long invoiceId) {
        updateInvoiceStatus(invoiceId, "CANCELLED");
    }
    
    /**
     * Xác nhận hóa đơn
     */
    public void confirmInvoice(Long invoiceId) {
        updateInvoiceStatus(invoiceId, "CONFIRMED");
    }
    
    /**
     * Hoàn thành hóa đơn
     */
    public void completeInvoice(Long invoiceId) {
        updateInvoiceStatus(invoiceId, "COMPLETED");
    }
    
    /**
     * Lấy các item trong hóa đơn
     */
    public List<ItemInvoice> getInvoiceItems(Long invoiceId) {
        return itemInvoiceRepository.findByInvoiceId(invoiceId);
    }
    
    /**
     * Đếm số lượng hóa đơn theo trạng thái
     */
    public Long countInvoicesByStatus(String status) {
        return invoiceRepository.countByStatus(status);
    }
}
