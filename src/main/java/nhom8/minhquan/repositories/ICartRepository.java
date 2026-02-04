package nhom8.minhquan.repositories;

import nhom8.minhquan.entities.CartEntity;
import nhom8.minhquan.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ICartRepository extends JpaRepository<CartEntity, Long> {
    Optional<CartEntity> findByUser(User user);
    Optional<CartEntity> findByUserId(Long userId);
    void deleteByUser(User user);
}
