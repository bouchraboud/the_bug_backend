package theBugApp.backend.service;

import theBugApp.backend.entity.Comment;
import theBugApp.backend.dto.CommentRequestDTO;
import theBugApp.backend.dto.CommentResponseDTO;

import java.util.List;

public interface CommentService {
    CommentResponseDTO addCommentToQuestion(Long questionId, String content, String userEmail);

    CommentResponseDTO addCommentToAnswer(Long answerId, String content, String userEmail);

    List<CommentResponseDTO> getCommentsForQuestion(Long questionId);

    List<CommentResponseDTO> getCommentsForAnswer(Long answerId);
}
