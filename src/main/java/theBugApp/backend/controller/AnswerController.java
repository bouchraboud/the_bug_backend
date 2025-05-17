
package theBugApp.backend.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
        import theBugApp.backend.dto.AnswerRequestDTO;
import theBugApp.backend.dto.AnswerResponseDTO;
import theBugApp.backend.exception.QuestionNotFoundException;
import theBugApp.backend.exception.UserNotFoundException;
import theBugApp.backend.service.AnswerService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/answers")
@AllArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    @PostMapping
    public ResponseEntity<?> createAnswer(
            @RequestBody AnswerRequestDTO answerRequest,
            @AuthenticationPrincipal Jwt jwt) {

        // Debug logging
        System.out.println("Creating answer for question ID: " + answerRequest.questionId());
        System.out.println("Answer content: " + answerRequest.content());

        Map<String, Object> claims = jwt.getClaim("claims");
        String email = (String) claims.get("email");
        System.out.println("User email: " + email);

        try {
            AnswerResponseDTO response = answerService.createAnswer(answerRequest, email);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (QuestionNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Question with ID " + answerRequest.questionId() + " does not exist");
        } catch (UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User with email " + email + " does not exist");
        }
    }

    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<AnswerResponseDTO>> getAnswersByQuestionId(@PathVariable Long questionId) {
        return ResponseEntity.ok(answerService.getAnswersByQuestionId(questionId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AnswerResponseDTO>> getAnswersByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(answerService.getAnswersByUserId(userId));
    }
}