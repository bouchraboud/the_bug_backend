package theBugApp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import theBugApp.backend.entity.FollowQuestion;
import theBugApp.backend.entity.FollowTag;
import theBugApp.backend.entity.Question;

import org.springframework.data.domain.Pageable;
import theBugApp.backend.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowQuestionRepository extends JpaRepository<FollowQuestion, Long> {
    // Changed from findByUserId to findByUserUserId to match the actual field name
    List<FollowQuestion> findByUserUserId(Long userId);

    // Added custom query for finding followers by question ID
    @Query("SELECT fq.user FROM FollowQuestion fq WHERE fq.question.id = :questionId")
    List<User> findFollowersByQuestionId(@Param("questionId") Long questionId);

    Optional<FollowQuestion> findByUserAndQuestion(User user, Question question);
}