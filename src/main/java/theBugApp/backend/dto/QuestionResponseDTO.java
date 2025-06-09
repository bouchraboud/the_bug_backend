package theBugApp.backend.dto;


import java.util.Date;
import java.util.List;
import java.util.Set;

public record QuestionResponseDTO(
        Long id,
        String title,
        String content,
        String plainTextContent,
        Date createdAt,
        Date updatedAt,
        UserDto user,
        int viewCount,
        int voteScore,
        int answerCount,  // Add this field
        Set<SimpleTagDTO> tags
) {}