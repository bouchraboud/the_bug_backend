package theBugApp.backend.mappers;

import org.springframework.stereotype.Component;
import theBugApp.backend.dto.NotificationDto;
import theBugApp.backend.entity.Notification;

@Component
public class NotificationMapper {
    public NotificationDto toDto(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setReferenceId(notification.getReferenceId());
        dto.setIsRead(notification.getIsRead());
        dto.setTimestamp(notification.getTimestamp());
        dto.setUserId(notification.getUser().getUserId());
        dto.setUsername(notification.getUser().getInfoUser().getUsername());
        return dto;
    }
}