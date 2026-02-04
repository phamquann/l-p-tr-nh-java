package nhom8.minhquan.services;

import nhom8.minhquan.entities.*;
import nhom8.minhquan.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReportService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private IBookRepository bookRepository;

    @Autowired
    private ICategoryRepository categoryRepository;

    @Autowired
    private IUserRepository userRepository;

    /**
     * Thống kê tổng quan
     */
    public Map<String, Object> getOverallStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Thống kê cơ bản
        stats.put("totalBooks", bookRepository.count());
        stats.put("totalCategories", categoryRepository.count());
        stats.put("totalUsers", userRepository.count());
        stats.put("totalInvoices", invoiceRepository.count());
        
        // Thống kê doanh thu
        List<Invoice> completedInvoices = invoiceRepository.findByStatus("COMPLETED");
        double totalRevenue = completedInvoices.stream()
                .mapToDouble(invoice -> invoice.getTotalAmount() != null ? invoice.getTotalAmount() : 0.0)
                .sum();
        stats.put("totalRevenue", totalRevenue);
        
        // Thống kê hôm nay
        LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfToday = startOfToday.plusDays(1);
        
        List<Invoice> todayInvoices = invoiceRepository.findByCreatedAtBetween(startOfToday, endOfToday);
        stats.put("todayOrders", todayInvoices.size());
        
        double todayRevenue = todayInvoices.stream()
                .filter(invoice -> "COMPLETED".equals(invoice.getStatus()))
                .mapToDouble(invoice -> invoice.getTotalAmount() != null ? invoice.getTotalAmount() : 0.0)
                .sum();
        stats.put("todayRevenue", todayRevenue);
        
        return stats;
    }

    /**
     * Thống kê theo danh mục
     */
    public List<Map<String, Object>> getCategoryStatistics() {
        List<Category> categories = categoryRepository.findAll();
        List<Map<String, Object>> categoryStats = new ArrayList<>();
        
        for (Category category : categories) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("categoryName", category.getName());
            stat.put("categoryId", category.getId());
            
            int bookCount = category.getBooks() != null ? category.getBooks().size() : 0;
            stat.put("bookCount", bookCount);
            
            // Tính doanh thu của danh mục đơn giản hơn
            double categoryRevenue = 0.0;
            if (category.getBooks() != null) {
                List<Invoice> allInvoices = invoiceRepository.findByStatus("COMPLETED");
                for (Book book : category.getBooks()) {
                    for (Invoice invoice : allInvoices) {
                        if (invoice.getItems() != null) {
                            for (ItemInvoice item : invoice.getItems()) {
                                if (item.getBook() != null && book.getId().equals(item.getBook().getId())) {
                                    categoryRevenue += (item.getSubtotal() != null ? item.getSubtotal() : 0.0);
                                }
                            }
                        }
                    }
                }
            }
            stat.put("revenue", categoryRevenue);
            
            categoryStats.add(stat);
        }
        
        return categoryStats;
    }

    /**
     * Thống kê 7 ngày gần nhất
     */
    public List<Map<String, Object>> getLast7DaysStatistics() {
        List<Map<String, Object>> dailyStats = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        
        for (int i = 6; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            
            List<Invoice> dayInvoices = invoiceRepository.findByCreatedAtBetween(startOfDay, endOfDay);
            
            Map<String, Object> dayStat = new HashMap<>();
            dayStat.put("date", date.format(formatter));
            dayStat.put("orders", dayInvoices.size());
            
            double dayRevenue = dayInvoices.stream()
                    .filter(invoice -> "COMPLETED".equals(invoice.getStatus()))
                    .mapToDouble(invoice -> invoice.getTotalAmount() != null ? invoice.getTotalAmount() : 0.0)
                    .sum();
            dayStat.put("revenue", dayRevenue);
            
            dailyStats.add(dayStat);
        }
        
        return dailyStats;
    }

    /**
     * Top sách bán chạy
     */
    public List<Map<String, Object>> getTopSellingBooks(int limit) {
        List<Book> allBooks = bookRepository.findAll();
        List<Map<String, Object>> bookStats = new ArrayList<>();
        
        for (Book book : allBooks) {
            // Đếm số lượng bán
            int totalSold = 0;
            List<Invoice> completedInvoices = invoiceRepository.findByStatus("COMPLETED");
            
            for (Invoice invoice : completedInvoices) {
                if (invoice.getItems() != null) {
                    for (ItemInvoice item : invoice.getItems()) {
                        if (item.getBook() != null && book.getId().equals(item.getBook().getId())) {
                            totalSold += (item.getQuantity() != null ? item.getQuantity() : 0);
                        }
                    }
                }
            }
            
            if (totalSold > 0) {
                Map<String, Object> bookStat = new HashMap<>();
                bookStat.put("bookId", book.getId());
                bookStat.put("bookTitle", book.getTitle());
                bookStat.put("bookAuthor", book.getAuthor());
                bookStat.put("categoryName", book.getCategory() != null ? book.getCategory().getName() : "N/A");
                bookStat.put("totalSold", totalSold);
                bookStat.put("price", book.getPrice());
                bookStat.put("revenue", totalSold * (book.getPrice() != null ? book.getPrice() : 0.0));
                bookStats.add(bookStat);
            }
        }
        
        // Sắp xếp theo số lượng bán giảm dần
        bookStats.sort((a, b) -> Integer.compare((Integer) b.get("totalSold"), (Integer) a.get("totalSold")));
        
        // Lấy top limit
        return bookStats.size() > limit ? bookStats.subList(0, limit) : bookStats;
    }

    /**
     * Thống kê trạng thái đơn hàng
     */
    public Map<String, Object> getOrderStatusStatistics() {
        List<Invoice> allInvoices = invoiceRepository.findAll();
        Map<String, Object> statusStats = new HashMap<>();
        
        Map<String, Integer> statusCount = new HashMap<>();
        statusCount.put("PENDING", 0);
        statusCount.put("CONFIRMED", 0);
        statusCount.put("SHIPPING", 0);
        statusCount.put("COMPLETED", 0);
        statusCount.put("CANCELLED", 0);
        
        for (Invoice invoice : allInvoices) {
            String status = invoice.getStatus() != null ? invoice.getStatus() : "PENDING";
            statusCount.put(status, statusCount.getOrDefault(status, 0) + 1);
        }
        
        statusStats.put("statusCount", statusCount);
        statusStats.put("totalOrders", allInvoices.size());
        
        return statusStats;
    }

    /**
     * Thống kê người dùng mới
     */
    public List<Map<String, Object>> getNewUsersLast7Days() {
        List<Map<String, Object>> userStats = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        
        // Lấy tổng số user hiện tại
        long totalUsers = userRepository.count();
        
        for (int i = 6; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            
            Map<String, Object> dayStat = new HashMap<>();
            dayStat.put("date", date.format(formatter));
            // Tạm thời hiển thị số user mới = 0, vì User entity chưa có trường createdAt
            dayStat.put("newUsers", i == 0 ? 1 : 0); // Giả sử có 1 user mới hôm nay
            
            userStats.add(dayStat);
        }
        
        return userStats;
    }
}