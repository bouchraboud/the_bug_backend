package theBugApp.backend.dto;

import java.util.Date;

public record AnswerRequestDTO(
        String content,
        Long questionId
) {}