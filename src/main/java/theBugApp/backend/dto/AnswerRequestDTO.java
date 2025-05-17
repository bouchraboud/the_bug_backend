package theBugApp.backend.dto;

public record AnswerRequestDTO(
        String content,
        Long questionId
) {}