package theBugApp.backend.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import theBugApp.backend.entity.User;
import theBugApp.backend.repository.UserRepository;
import theBugApp.backend.service.FollowService;

import java.util.Map;

@RestController
@RequestMapping("/api/follow")
@AllArgsConstructor
public class FollowController {

    private final FollowService followService;
    private final UserRepository userRepository;

    @PostMapping("/tags/{id}")
    public ResponseEntity<?> followTag(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> claims = jwt.getClaim("claims");
        String email = (String) claims.get("email");
        followService.followTag(id, email);
        return ResponseEntity.ok("Tag followed successfully");
    }

    @PostMapping("/questions/{id}")
    public ResponseEntity<?> followQuestion(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> claims = jwt.getClaim("claims");
        String email = (String) claims.get("email");
        followService.followQuestion(id, email);
        return ResponseEntity.ok("Question followed successfully");
    }

    @PostMapping("/answers/{id}")
    public ResponseEntity<?> followAnswer(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> claims = jwt.getClaim("claims");
        String email = (String) claims.get("email");
        followService.followAnswer(id, email);
        return ResponseEntity.ok("Answer followed successfully");
    }
    @DeleteMapping("/tags/{id}")
    public ResponseEntity<?> unfollowTag(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> claims = jwt.getClaim("claims");
        String email = (String) claims.get("email");
        System.out.println(email);
        followService.unfollowTag(id, email);
        return ResponseEntity.ok("Tag unfollowed successfully");
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<?> unfollowQuestion(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> claims = jwt.getClaim("claims");
        String email = (String) claims.get("email");
        followService.unfollowQuestion(id, email);
        return ResponseEntity.ok("Question unfollowed successfully");
    }

    @DeleteMapping("/answers/{id}")
    public ResponseEntity<?> unfollowAnswer(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> claims = jwt.getClaim("claims");
        String email = (String) claims.get("email");
        followService.unfollowAnswer(id, email);
        return ResponseEntity.ok("Answer unfollowed successfully");
    }
    // NEW GET endpoints
    @GetMapping("/tags")
    public ResponseEntity<?> getFollowedTags(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> claims = jwt.getClaim("claims");
        String email = (String) claims.get("email");
        userRepository.findByInfoUser_Email(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(followService.getFollowedTags(email));
    }

    @GetMapping("/questions")
    public ResponseEntity<?> getFollowedQuestions(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> claims = jwt.getClaim("claims");
        String email = (String) claims.get("email");

         userRepository.findByInfoUser_Email(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(followService.getFollowedQuestions(email));
    }

    @GetMapping("/answers")
    public ResponseEntity<?> getFollowedAnswers(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> claims = jwt.getClaim("claims");
        String email = (String) claims.get("email");

        userRepository.findByInfoUser_Email(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(followService.getFollowedAnswers(email));
    }

}
