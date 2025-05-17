package theBugApp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import theBugApp.backend.entity.Question;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.tags WHERE q.id = :id")
    Optional<Question> findByIdWithTags(Long id);

    // Add method to find all questions with tags loaded
    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.tags")
    List<Question> findAllWithTags();

    @Query("SELECT DISTINCT q FROM Question q JOIN q.tags t WHERE t.name = :tagName")
    List<Question> findByTagName(@Param("tagName") String tagName);

    @Query("SELECT q FROM Question q WHERE q.user.userId = :userId")
    List<Question> findByUserId(@Param("userId") Long userId);
}
