package theBugApp.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import theBugApp.backend.dto.*;
import theBugApp.backend.entity.User;
import theBugApp.backend.repository.UserRepository;
import theBugApp.backend.service.ReputationService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reputation")
@RequiredArgsConstructor
public class ReputationController {

    private final ReputationService reputationService;
    private final UserRepository userRepository;
    @GetMapping("/users/{userId}")
    public ResponseEntity<Integer> getUserReputation(@PathVariable Long userId) {
        return ResponseEntity.ok(reputationService.getUserReputation(userId));
    }

    @GetMapping("/users/history")
    public ResponseEntity<List<ReputationHistoryDTO>> getUserReputationHistory(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> claims = jwt.getClaim("claims");
        String email = (String) claims.get("email");

        User user = userRepository.findByInfoUser_Email(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(reputationService.getUserReputationHistory(user.getUserId()));
    }

    @GetMapping("/users/{userId}/privileges")
    public ResponseEntity<Map<String, Boolean>> getUserPrivileges(@PathVariable Long userId) {
        Map<String, Boolean> privileges = new HashMap<>();
        privileges.put("canUpvote", reputationService.canUpvote(userId));
        privileges.put("canDownvote", reputationService.canDownvote(userId));
        privileges.put("canComment", reputationService.canComment(userId));
        privileges.put("canCreateTags", reputationService.canCreateTags(userId));
        privileges.put("canEdit", reputationService.canEdit(userId));
        privileges.put("canDelete", reputationService.canDelete(userId));
        privileges.put("canModerate", reputationService.canModerate(userId));
        return ResponseEntity.ok(privileges);
    }

    @GetMapping("/users/daily-limit")
    public ResponseEntity<Map<String, Object>> getDailyLimitInfo(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> claims = jwt.getClaim("claims");
        String email = (String) claims.get("email");

        User user = userRepository.findByInfoUser_Email(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Map<String, Object> info = new HashMap<>();
        info.put("dailyEarned", reputationService.getDailyReputationEarned(user.getUserId()));
        info.put("hasReachedLimit", reputationService.hasReachedDailyLimit(user.getUserId()));
        info.put("limit", 200);
        return ResponseEntity.ok(info);
    }
}