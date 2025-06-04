package theBugApp.backend.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import theBugApp.backend.dto.NotificationDto;
import theBugApp.backend.entity.User;
import theBugApp.backend.mappers.NotificationMapper;
import theBugApp.backend.repository.UserRepository;
import theBugApp.backend.service.NotificationService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@AllArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getNotifications(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> claims = jwt.getClaim("claims");
        String email = (String) claims.get("email");

        User user = userRepository.findByInfoUser_Email(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<NotificationDto> notifications = notificationService.getNotificationsForUser(user.getUserId())
                .stream()
                .map(notificationMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> claims = jwt.getClaim("claims");
        String email = (String) claims.get("email");

        User user = userRepository.findByInfoUser_Email(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<NotificationDto> notifications = notificationService.getUnreadNotificationsForUser(user.getUserId())
                .stream()
                .map(notificationMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<String> markAsRead(@PathVariable Long notificationId,@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> claims = jwt.getClaim("claims");
        String email = (String) claims.get("email");
        User user = userRepository.findByInfoUser_Email(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        notificationService.markNotificationAsRead(notificationId);
        return ResponseEntity.ok("Notification marked as read");
    }

    @PutMapping("/read-all")
    public ResponseEntity<String> markAllAsRead(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> claims = jwt.getClaim("claims");
        String email = (String) claims.get("email");

        User user = userRepository.findByInfoUser_Email(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        notificationService.markAllNotificationsAsRead(user.getUserId());
        return ResponseEntity.ok("All notifications marked as read");
    }

    @GetMapping("/count/unread")
    public ResponseEntity<Integer> getUnreadCount(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> claims = jwt.getClaim("claims");
        String email = (String) claims.get("email");

        User user = userRepository.findByInfoUser_Email(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int count = notificationService.getUnreadNotificationsForUser(user.getUserId()).size();
        return ResponseEntity.ok(count);
    }
}