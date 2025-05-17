package theBugApp.backend.dto;


import java.util.Date;
import java.util.Set;

public record QuestionResponseDTO(
        Long id,
        String title,
        String content,
        Date createdAt,
        Date updatedAt,
        String authorUsername,
        String authorEmail,
        int viewCount,
        int voteScore,
        Set<SimpleTagDTO> tags// Plus tard: List<TagDTO> tags, List<AnswerDTO> answers, etc.
) {}