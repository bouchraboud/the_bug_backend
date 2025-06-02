
package theBugApp.backend.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
        import theBugApp.backend.dto.AnswerRequestDTO;
import theBugApp.backend.dto.AnswerResponseDTO;
import theBugApp.backend.exception.AnswerNotFoundException;
import theBugApp.backend.exception.QuestionNotFoundException;
import theBugApp.backend.exception.UnauthorizedActionException;
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

    @PostMapping("/{id}/accept")
    public ResponseEntity<?> acceptAnswer(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        Map<String, Object> claims = jwt.getClaim("claims");
        String email = (String) claims.get("email");

        try {
            AnswerResponseDTO response = answerService.acceptAnswer(id, email);
            return ResponseEntity.ok(response);
        } catch (AnswerNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Answer with ID " + id + " does not exist");
        } catch (UnauthorizedActionException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not authorized to accept this answer");
        }
    }
    @PostMapping("/{id}/disaccept")
    public ResponseEntity<?> disacceptAnswer(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        Map<String, Object> claims = jwt.getClaim("claims");
        String email = (String) claims.get("email");

        try {
            AnswerResponseDTO response = answerService.disacceptAnswer(id, email);
            return ResponseEntity.ok(response);
        } catch (AnswerNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Answer with ID " + id + " does not exist");
        } catch (UnauthorizedActionException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not authorized to disaccept this answer");
        }
    }


}