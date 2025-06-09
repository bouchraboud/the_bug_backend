package theBugApp.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import theBugApp.backend.entity.Vote;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteInfoDto {
    private Long userId;
    private Vote.VoteType voteType;
}
