package theBugApp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import theBugApp.backend.entity.*;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowAnswerRepository extends JpaRepository<FollowAnswer, Long> {
    // Changed from findByUserId to findByUserUserId to match the actual field name
    List<FollowAnswer> findByUserUserId(Long userId);

    // Added custom query for finding followers by answer ID
    @Query("SELECT fa.user FROM FollowAnswer fa WHERE fa.answer.id = :answerId")
    List<User> findFollowersByAnswerId(@Param("answerId") Long answerId);
    Optional<FollowAnswer> findByUserAndAnswer(User user, Answer answer);

    boolean existsByUserAndAnswer(User user, Answer answer);

}