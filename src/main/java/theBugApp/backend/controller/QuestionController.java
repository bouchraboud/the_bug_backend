package theBugApp.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import theBugApp.backend.dto.AnswerResponseDTO;
import theBugApp.backend.dto.QuestionRequestDTO;
import theBugApp.backend.dto.QuestionResponseDTO;
import theBugApp.backend.service.AnswerService;
import theBugApp.backend.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final AnswerService answerService;

    @PostMapping
    public ResponseEntity<QuestionResponseDTO> createQuestion(
            @RequestBody QuestionRequestDTO questionRequestDTO,
            @AuthenticationPrincipal Jwt jwt) {

        // Debug logging
        System.out.println("Creating question with title: " + questionRequestDTO.title());
        if (questionRequestDTO.tagNames() != null) {
            System.out.println("Tags submitted: " + String.join(", ", questionRequestDTO.tagNames()));
        } else {
            System.out.println("No tags submitted");
        }

        Map<String, Object> claims = jwt.getClaim("claims");
        String email = (String) claims.get("email");
        System.out.println("User email: " + email);

        QuestionResponseDTO response = questionService.createQuestion(questionRequestDTO, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }




    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponseDTO> getQuestion(@PathVariable Long id) {
        QuestionResponseDTO response = questionService.getQuestionById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<QuestionResponseDTO>> getAllQuestions() {
        List<QuestionResponseDTO> responses = questionService.getAllQuestions();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{questionId}/answers")
    public ResponseEntity<List<AnswerResponseDTO>> getAnswersForQuestion(
            @PathVariable Long questionId) {
        return ResponseEntity.ok(answerService.getAnswersByQuestionId(questionId));
    }

    // Autres endpoints à ajouter selon les besoins...

    @GetMapping("/search")
    public ResponseEntity<List<QuestionResponseDTO>> searchQuestions(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<QuestionResponseDTO> responses = questionService.searchQuestions(query, tag, page, size);
        return ResponseEntity.ok(responses);
    }
    // Add these methods to your QuestionController.java
    @PutMapping("/{questionId}")
    public ResponseEntity<QuestionResponseDTO> updateQuestion(
            @PathVariable Long questionId,
            @RequestBody QuestionRequestDTO questionRequest,
            @AuthenticationPrincipal Jwt jwt) {
        System.out.println("Updating question with id: " + questionId);
        System.out.println("Updating question request: " + questionRequest);
        System.out.println(jwt);
        Map<String, Object> claims = jwt.getClaim("claims");
        String email = (String) claims.get("email");
        System.out.println(jwt);

        QuestionResponseDTO updatedQuestion = questionService.updateQuestion(questionId, questionRequest, email);
        return ResponseEntity.ok(updatedQuestion);
    }

}