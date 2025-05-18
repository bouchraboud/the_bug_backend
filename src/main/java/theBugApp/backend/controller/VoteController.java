package theBugApp.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import theBugApp.backend.entity.Vote;
import theBugApp.backend.service.VoteService;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/votes")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    @PostMapping("/questions/{questionId}/upvote")
    public ResponseEntity<Integer> upvoteQuestion(
            @PathVariable Long questionId,
            @AuthenticationPrincipal Jwt jwt) {

        // Extraction de l'email comme dans votre QuestionController
        Map<String, Object> claims = jwt.getClaim("claims");
        String email = (String) claims.get("email");
        System.out.println("User email from JWT: " + email);

        int voteScore = voteService.voteQuestion(questionId, email, Vote.VoteType.UPVOTE);
        return ResponseEntity.ok(voteScore);
    }

    @PostMapping("/questions/{questionId}/downvote")
    public ResponseEntity<Integer> downvoteQuestion(
            @PathVariable Long questionId,
            @AuthenticationPrincipal Jwt jwt) {

        // Extraction de l'email comme dans votre QuestionController
        Map<String, Object> claims = jwt.getClaim("claims");
        String email = (String) claims.get("email");
        System.out.println("User email from JWT: " + email);

        int voteScore = voteService.voteQuestion(questionId, email, Vote.VoteType.DOWNVOTE);
        return ResponseEntity.ok(voteScore);
    }



    @PostMapping("/answers/{answerId}/upvote")
    public ResponseEntity<Integer> upvoteAnswer(
            @PathVariable Long answerId,
            @AuthenticationPrincipal Jwt jwt) {

        // Extraction de l'email comme dans votre QuestionController
        Map<String, Object> claims = jwt.getClaim("claims");
        String email = (String) claims.get("email");
        System.out.println("User email from JWT: " + email);

        int voteScore = voteService.voteAnswer(answerId, email, Vote.VoteType.UPVOTE);
        return ResponseEntity.ok(voteScore);
    }

    @PostMapping("/answers/{answerId}/downvote")
    public ResponseEntity<Integer> downvoteAnswer(
            @PathVariable Long answerId,
            @AuthenticationPrincipal Jwt jwt) {

        // Extraction de l'email comme dans votre QuestionController
        Map<String, Object> claims = jwt.getClaim("claims");
        String email = (String) claims.get("email");
        System.out.println("User email from JWT: " + email);

        int voteScore = voteService.voteAnswer(answerId, email, Vote.VoteType.DOWNVOTE);
        return ResponseEntity.ok(voteScore);
    }
}