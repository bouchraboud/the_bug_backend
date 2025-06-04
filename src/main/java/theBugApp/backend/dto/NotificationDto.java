package theBugApp.backend.dto;

import lombok.Data;
import theBugApp.backend.enums.NotificationType;
import java.time.LocalDateTime;

@Data
public class NotificationDto {
    private Long id;
    private String message;
    private NotificationType type;
    private Long referenceId;
    private Boolean isRead;
    private LocalDateTime timestamp;
    private Long userId;
    private String username;
}