package theBugApp.backend.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import theBugApp.backend.entity.UserConfirmationToken;

import java.util.Optional;

@Repository
public interface UserConfirmationTokenRepo extends JpaRepository<UserConfirmationToken, Long> {
    Optional<UserConfirmationToken> findByToken(String token);
}