package theBugApp.backend.service;

import theBugApp.backend.dto.AnswerRequestDTO;
import theBugApp.backend.dto.AnswerResponseDTO;

import java.util.List;

public interface AnswerService {
    AnswerResponseDTO createAnswer(AnswerRequestDTO answerRequest, String userEmail);
    List<AnswerResponseDTO> getAnswersByQuestionId(Long questionId);
    List<AnswerResponseDTO> getAnswersByUserId(Long userId);
}