package theBugApp.backend.service;

import theBugApp.backend.dto.QuestionRequestDTO;
import theBugApp.backend.dto.QuestionResponseDTO;
import theBugApp.backend.entity.Question;

import java.util.List;
public interface QuestionService {
    QuestionResponseDTO createQuestion(QuestionRequestDTO questionRequestDTO, String userEmail); // Ajout du paramètre userEmail
    QuestionResponseDTO getQuestionById(Long id);
    List<QuestionResponseDTO> getAllQuestions();
}