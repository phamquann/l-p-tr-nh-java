package nhom8.minhquan.repositories;

import nhom8.minhquan.entities.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    // Tìm sách theo tên (tìm kiếm không phân biệt chữ hoa/thường)
    List<Book> findByTitleContainingIgnoreCase(String title);
    
    // Tìm sách theo tác giả
    List<Book> findByAuthorContainingIgnoreCase(String author);
    
    // Tìm sách theo thể loại
    List<Book> findByCategoryContainingIgnoreCase(String category);
    
    // Tìm sách có giá trong khoảng
    List<Book> findByPriceBetween(Double minPrice, Double maxPrice);
}
