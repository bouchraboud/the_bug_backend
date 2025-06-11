package theBugApp.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import theBugApp.backend.entity.Question;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
@Repository
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
    // Count the number of questions by user
    @Query("SELECT COUNT(q) FROM Question q WHERE q.user.userId = :userId")
    long countByUserId(@Param("userId") Long userId);
    // Search by title or content with pagination
    @Query("SELECT q FROM Question q WHERE LOWER(q.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(q.content) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Question> searchQuestionsByTitleOrContent(@Param("query") String query, Pageable pageable);

    // Search by title or content and filter by tag
    @Query("SELECT q FROM Question q JOIN q.tags t WHERE (LOWER(q.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(q.content) LIKE LOWER(CONCAT('%', :query, '%'))) AND LOWER(t.name) = LOWER(:tag)")
    List<Question> searchByTitleOrContentAndTag(@Param("query") String query, @Param("tag") String tag, Pageable pageable);

    // Filter by tag only with pagination
    List<Question> findByTags_Name(String tag, Pageable pageable);

    Page<Question> findAllByOrderByVoteScoreDesc(Pageable pageable);
    Page<Question> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Question> findAllByOrderByUpdatedAtDesc(Pageable pageable);
    Page<Question> findAllByOrderByTitleAsc(Pageable pageable);
}
