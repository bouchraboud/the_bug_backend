package theBugApp.backend.dto;

import java.time.LocalDateTime;

public record ReputationHistoryDTO(
        Long id,
        String action,
        int points,
        String description,
        LocalDateTime createdAt,
        Long questionId,
        Long answerId
) {}