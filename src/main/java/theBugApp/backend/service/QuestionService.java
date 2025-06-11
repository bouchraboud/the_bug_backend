package theBugApp.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import theBugApp.backend.dto.QuestionRequestDTO;
import theBugApp.backend.dto.QuestionResponseDTO;
import theBugApp.backend.entity.Question;

import java.util.List;
public interface QuestionService {
    QuestionResponseDTO createQuestion(QuestionRequestDTO questionRequestDTO, String userEmail); // Ajout du paramètre userEmail
    QuestionResponseDTO getQuestionById(Long id);
    List<QuestionResponseDTO> getAllQuestions();

    Page<QuestionResponseDTO> getAllQuestions(Pageable pageable, String sortBy);

    Page<QuestionResponseDTO> getAllQuestions(Pageable pageable);

    QuestionResponseDTO convertToResponseDTO(Question question);

    @Transactional(readOnly = true)
    List<QuestionResponseDTO> searchQuestions(String query, String tag, int page, int size);

    // Add this method if you plan to implement question updates
    @Transactional
    QuestionResponseDTO updateQuestion(Long questionId, QuestionRequestDTO request, String userEmail);
}