package nhom8.minhquan.repositories;

import nhom8.minhquan.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ICategoryRepository extends
JpaRepository<Category, Long> {
}