package theBugApp.backend.service;

import org.springframework.transaction.annotation.Transactional;
import theBugApp.backend.dto.AnswerRequestDTO;
import theBugApp.backend.dto.AnswerResponseDTO;

import java.util.List;

public interface AnswerService {
    AnswerResponseDTO createAnswer(AnswerRequestDTO answerRequest, String userEmail);
    List<AnswerResponseDTO> getAnswersByQuestionId(Long questionId);
    List<AnswerResponseDTO> getAnswersByUserId(Long userId);

    AnswerResponseDTO acceptAnswer(Long answerId, String userEmail);

    AnswerResponseDTO disacceptAnswer(Long answerId, String userEmail);

    // Add this method if you plan to implement answer updates
    @Transactional
    AnswerResponseDTO updateAnswer(Long answerId, AnswerRequestDTO answerRequest, String userEmail);
}