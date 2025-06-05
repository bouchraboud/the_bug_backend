package theBugApp.backend.service;

import theBugApp.backend.dto.ReputationHistoryDTO;
import theBugApp.backend.entity.User;
import theBugApp.backend.enums.ReputationAction;

import java.time.LocalDate;
import java.util.List;

public interface ReputationService {

    void awardReputation(Long userId, ReputationAction action, Long questionId, Long answerId, Long voteId);
    void awardReputation(Long userId, ReputationAction action);
    void awardCustomReputation(Long userId, int points, String description, Long questionId, Long answerId);

    int getUserReputation(Long userId);
    List<ReputationHistoryDTO> getUserReputationHistory(Long userId);
    List<ReputationHistoryDTO> getUserReputationHistoryForPeriod(Long userId, LocalDate startDate, LocalDate endDate);

    boolean hasReachedDailyLimit(Long userId);
    int getDailyReputationEarned(Long userId);

    // Reputation thresholds for privileges
    boolean canUpvote(Long userId); // 15 rep
    boolean canDownvote(Long userId); // 125 rep
    boolean canComment(Long userId); // 50 rep
    boolean canCreateTags(Long userId); // 1500 rep
    boolean canEdit(Long userId); // 2000 rep
    boolean canDelete(Long userId); // 10000 rep
    boolean canModerate(Long userId); // 10000 rep
}
