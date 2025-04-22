package theBugApp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import theBugApp.backend.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByInfoUser_Email(String email);
    boolean existsByInfoUser_Username(String username);

    Optional<User> findByInfoUser_Email(String email);
    Optional<User> findById(Long id); // plus de cast de String
}
