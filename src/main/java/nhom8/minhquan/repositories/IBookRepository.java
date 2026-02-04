package nhom8.minhquan.repositories;

import nhom8.minhquan.entities.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IBookRepository extends JpaRepository<Book, Long> {

	@Query("select b from Book b left join fetch b.category")
	List<Book> findAllWithCategory();
}
