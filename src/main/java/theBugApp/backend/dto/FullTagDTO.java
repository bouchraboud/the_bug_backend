package theBugApp.backend.dto;

import java.util.List;

// For admin features
public record FullTagDTO(
        Long id,
        String name,
        int usageCount,
        int followersCount,
        List<UserDto> followers,
        List<QuestionResponseDTO> questions

) {}
