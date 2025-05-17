package theBugApp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import theBugApp.backend.entity.Tag;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
    // You can add more methods to find questions by tag


    @Query("SELECT t.name FROM Tag t JOIN t.questions q WHERE q.id = :questionId")
    Set<String> findTagsByQuestionId(@Param("questionId") Long questionId);

    @Query("SELECT t.name FROM Tag t JOIN t.questions q WHERE q.id = :questionId")
    Set<String> findTagNamesByQuestionId(@Param("questionId") Long questionId);







    @Query("SELECT t FROM Tag t WHERE t.name IN :names")
    List<Tag> findByNameIn(@Param("names") Set<String> names);


    @Query("SELECT q.id, t.name FROM Question q JOIN q.tags t WHERE q.id IN :questionIds")
    List<Object[]> findTagNamesForQuestionIds(@Param("questionIds") List<Long> questionIds);
}

