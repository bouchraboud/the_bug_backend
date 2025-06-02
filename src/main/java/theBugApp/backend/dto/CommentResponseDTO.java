package theBugApp.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CommentResponseDTO {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private String username;
}
