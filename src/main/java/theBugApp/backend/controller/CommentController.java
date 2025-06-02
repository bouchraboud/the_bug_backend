package theBugApp.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import theBugApp.backend.dto.CommentRequestDTO;
import theBugApp.backend.dto.CommentResponseDTO;
import theBugApp.backend.service.CommentService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/questions/{questionId}")
    public ResponseEntity<CommentResponseDTO> addCommentToQuestion(
            @PathVariable Long questionId,
            @RequestBody @Valid CommentRequestDTO commentRequestDTO,
            @AuthenticationPrincipal Jwt jwt) {

        Map<String, Object> claims = jwt.getClaim("claims");
        String email = (String) claims.get("email");
        CommentResponseDTO response = commentService.addCommentToQuestion(
                questionId,
                commentRequestDTO.getContent(),
                email
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/answers/{answerId}")
    public ResponseEntity<CommentResponseDTO> addCommentToAnswer(
            @PathVariable Long answerId,
            @RequestBody @Valid CommentRequestDTO commentRequestDTO,
            @AuthenticationPrincipal Jwt jwt) {

        Map<String, Object> claims = jwt.getClaim("claims");
        String email = (String) claims.get("email");
        CommentResponseDTO response = commentService.addCommentToAnswer(
                answerId,
                commentRequestDTO.getContent(),
                email
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



    @GetMapping("/questions/{questionId}")
    public ResponseEntity<List<CommentResponseDTO>> getCommentsForQuestion(
            @PathVariable Long questionId) {
        List<CommentResponseDTO> responses = commentService.getCommentsForQuestion(questionId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/answers/{answerId}")
    public ResponseEntity<List<CommentResponseDTO>> getCommentsForAnswer(
            @PathVariable Long answerId) {
        List<CommentResponseDTO> responses = commentService.getCommentsForAnswer(answerId);
        return ResponseEntity.ok(responses);
    }
}
