package theBugApp.backend.dto;

import java.util.Date;

public record AnswerResponseDTO(
        Long id,
        String content,
        Date createdAt,
        Date updatedAt,
        boolean isAccepted,
        int voteScore,
        UserDto user,
        Long questionId
) {}