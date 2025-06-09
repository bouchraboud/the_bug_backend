
package theBugApp.backend.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
        import theBugApp.backend.dto.AnswerRequestDTO;
import theBugApp.backend.dto.AnswerResponseDTO;
import theBugApp.backend.dto.VoteInfoDto;
import theBugApp.backend.exception.AnswerNotFoundException;
import theBugApp.backend.exception.QuestionNotFoundException;
import theBugApp.backend.exception.UnauthorizedActionException;
import theBugApp.backend.exception.UserNotFoundException;
import theBugApp.backend.service.AnswerService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import theBugApp.backend.service.VoteServiceImpl;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/answers")
@AllArgsConstructor
public class AnswerController {

    private final AnswerService answerService;
    private final VoteServiceImpl voteService;

    @PostMapping
    public ResponseEntity<?> createAnswer(
            @RequestBody AnswerRequestDTO answerRequest,
            @AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> claims = jwt.getClaim("claims");
        String email = (String) claims.get("email");
        try {
            AnswerResponseDTO response = answerService.createAnswer(answerRequest, email);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (QuestionNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Question with ID " + answerRequest.questionId() + " does not exist"));
        } catch (UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
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
                    .body(Map.of("message", "Answer with ID " + id + " does not exist"));
        } catch (UnauthorizedActionException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You are not authorized to accept this answer"));
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
                    .body(Map.of("message","Answer with ID " + id + " does not exist"));
        } catch (UnauthorizedActionException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message","You are not authorized to disaccept this answer"));
        }
    }
    // Add these methods to your AnswerController.java
    @PutMapping("/{answerId}")
    public ResponseEntity<AnswerResponseDTO> updateAnswer(
            @PathVariable Long answerId,
            @RequestBody AnswerRequestDTO answerRequest,
            @AuthenticationPrincipal Jwt jwt) {

        Map<String, Object> claims = jwt.getClaim("claims");
        String email = (String) claims.get("email");

        AnswerResponseDTO updatedAnswer = answerService.updateAnswer(answerId, answerRequest, email);
        return ResponseEntity.ok(updatedAnswer);
    }
    @GetMapping("/{answerId}/voters")
    public ResponseEntity<List<VoteInfoDto>> getVoteInfoByAnswer(@PathVariable Long answerId) {
        List<VoteInfoDto> voteInfo = voteService.getVoteInfoByAnswer(answerId);
        return ResponseEntity.ok(voteInfo);
    }


}