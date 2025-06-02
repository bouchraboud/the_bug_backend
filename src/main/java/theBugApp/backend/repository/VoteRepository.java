package theBugApp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import theBugApp.backend.entity.Vote;
import theBugApp.backend.entity.User;
import theBugApp.backend.entity.Question;
import theBugApp.backend.entity.Answer;

import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByUserAndQuestion(User user, Question question);
    Optional<Vote> findByUserAndAnswer(User user, Answer answer);
    boolean existsByUserAndQuestion(User user, Question question);
    boolean existsByUserAndAnswer(User user, Answer answer);
    // Add this new method
    List<Vote> findByQuestion(Question question);
    List<Vote> findByAnswer(Answer answer);

    // Count total votes by user (both question and answer votes)
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.user.userId = :userId")
    long countByUserId(@Param("userId") Long userId);

    // Count votes on questions by user
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.user.userId = :userId AND v.question IS NOT NULL")
    long countQuestionVotesByUserId(@Param("userId") Long userId);

    // Count votes on answers by user
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.user.userId = :userId AND v.answer IS NOT NULL")
    long countAnswerVotesByUserId(@Param("userId") Long userId);

    // Count upvotes by user
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.user.userId = :userId AND v.voteType = 'UPVOTE'")
    long countUpvotesByUserId(@Param("userId") Long userId);

    // Count downvotes by user
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.user.userId = :userId AND v.voteType = 'DOWNVOTE'")
    long countDownvotesByUserId(@Param("userId") Long userId);

}