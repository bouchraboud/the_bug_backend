package theBugApp.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record QuestionRequestDTO(
        @NotBlank String title,
        @NotBlank String content,
        Set<@NotBlank String> tagNames  // e.g., ["java", "spring"]

) {}