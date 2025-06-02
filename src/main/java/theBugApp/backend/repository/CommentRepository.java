package theBugApp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import theBugApp.backend.entity.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByQuestionId(Long questionId);

    List<Comment> findByAnswerId(Long answerId);
}
