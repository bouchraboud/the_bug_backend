package theBugApp.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import theBugApp.backend.dto.CommentRequestDTO;
import theBugApp.backend.dto.CommentResponseDTO;
import theBugApp.backend.entity.*;
import theBugApp.backend.exception.AnswerNotFoundException;
import theBugApp.backend.exception.QuestionNotFoundException;
import theBugApp.backend.exception.UserNotFoundException;
import theBugApp.backend.repository.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CommentResponseDTO addCommentToQuestion(Long questionId, String content, String userEmail) {
        User user = userRepository.findByInfoUser_Email(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userEmail));
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException(questionId)); // pass Long

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(user);
        comment.setQuestion(question);

        Comment savedComment = commentRepository.save(comment);
        return convertToResponseDTO(savedComment);
    }

    @Override
    @Transactional
    public CommentResponseDTO addCommentToAnswer(Long answerId, String content, String userEmail) {
        User user = userRepository.findByInfoUser_Email(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userEmail));
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new AnswerNotFoundException(answerId));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(user);
        comment.setAnswer(answer);

        Comment savedComment = commentRepository.save(comment);
        return convertToResponseDTO(savedComment);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CommentResponseDTO> getCommentsForQuestion(Long questionId) {
        return commentRepository.findByQuestionId(questionId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<CommentResponseDTO> getCommentsForAnswer(Long answerId) {
        return commentRepository.findByAnswerId(answerId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    private CommentResponseDTO convertToResponseDTO(Comment comment) {
        return new CommentResponseDTO(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUser().getInfoUser().getUsername()
        );
    }
}
