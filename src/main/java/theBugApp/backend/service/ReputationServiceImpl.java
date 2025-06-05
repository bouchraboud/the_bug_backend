package theBugApp.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import theBugApp.backend.dto.ReputationHistoryDTO;
import theBugApp.backend.entity.ReputationHistory;
import theBugApp.backend.entity.User;
import theBugApp.backend.enums.ReputationAction;
import theBugApp.backend.exception.UserNotFoundException;
import theBugApp.backend.repository.ReputationHistoryRepository;
import theBugApp.backend.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReputationServiceImpl implements ReputationService {

    private final ReputationHistoryRepository reputationHistoryRepository;
    private final UserRepository userRepository;

    private static final int DAILY_REPUTATION_LIMIT = 200;
    private static final int MIN_REPUTATION_TO_UPVOTE = 15;
    private static final int MIN_REPUTATION_TO_DOWNVOTE = 125;
    private static final int MIN_REPUTATION_TO_COMMENT = 50;
    private static final int MIN_REPUTATION_TO_CREATE_TAGS = 1500;
    private static final int MIN_REPUTATION_TO_EDIT = 2000;
    private static final int MIN_REPUTATION_TO_DELETE = 10000;

    @Override
    public void awardReputation(Long userId, ReputationAction action, Long questionId, Long answerId, Long voteId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        int points = action.getPoints();

        // Check daily limit for positive reputation gains
        if (points > 0 && hasReachedDailyLimit(userId)) {
            return; // Don't award reputation if daily limit reached
        }

        // Special handling for downvote penalties (different user)
        if (action == ReputationAction.DOWNVOTE_GIVEN) {
            // This affects the user who gave the downvote, not the receiver
            points = -1;
        }

        // Create reputation history record
        ReputationHistory history = new ReputationHistory();
        history.setUser(user);
        history.setAction(action);
        history.setPoints(points);
        history.setQuestionId(questionId);
        history.setAnswerId(answerId);
        history.setVoteId(voteId);
        history.setDescription(action.getDescription());

        reputationHistoryRepository.save(history);

        // Update user's total reputation
        user.setReputation(Math.max(1, user.getReputation() + points)); // Minimum reputation is 1
        userRepository.save(user);
    }
    @Override
    public void awardReputation(Long userId, ReputationAction action) {
        awardReputation(userId, action, null, null, null);
    }

    @Override
    public void awardCustomReputation(Long userId, int points, String description, Long questionId, Long answerId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // Check daily limit for positive reputation gains
        if (points > 0 && hasReachedDailyLimit(userId)) {
            return;
        }

        ReputationHistory history = new ReputationHistory();
        history.setUser(user);
        history.setAction(ReputationAction.BOUNTY_AWARDED); // Use generic action for custom points
        history.setPoints(points);
        history.setQuestionId(questionId);
        history.setAnswerId(answerId);
        history.setDescription(description);

        reputationHistoryRepository.save(history);

        user.setReputation(Math.max(1, user.getReputation() + points));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public int getUserReputation(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        return user.getReputation();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReputationHistoryDTO> getUserReputationHistory(Long userId) {
        List<ReputationHistory> history = reputationHistoryRepository.findByUserUserIdOrderByCreatedAtDesc(userId);
        return history.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReputationHistoryDTO> getUserReputationHistoryForPeriod(Long userId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        List<ReputationHistory> history = reputationHistoryRepository.findUserReputationSince(userId, startDateTime);
        return history.stream()
                .filter(h -> h.getCreatedAt().isBefore(endDateTime))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasReachedDailyLimit(Long userId) {
        return getDailyReputationEarned(userId) >= DAILY_REPUTATION_LIMIT;
    }

    @Override
    @Transactional(readOnly = true)
    public int getDailyReputationEarned(Long userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay();

        Integer totalPoints = reputationHistoryRepository.getTotalPointsForUserInPeriod(userId, startOfDay, endOfDay);
        return Math.max(0, totalPoints != null ? totalPoints : 0); // Only count positive gains toward limit
    }

    // Privilege checking methods
    @Override
    @Transactional(readOnly = true)
    public boolean canUpvote(Long userId) {
        return getUserReputation(userId) >= MIN_REPUTATION_TO_UPVOTE;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDownvote(Long userId) {
        return getUserReputation(userId) >= MIN_REPUTATION_TO_DOWNVOTE;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canComment(Long userId) {
        return getUserReputation(userId) >= MIN_REPUTATION_TO_COMMENT;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canCreateTags(Long userId) {
        return getUserReputation(userId) >= MIN_REPUTATION_TO_CREATE_TAGS;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canEdit(Long userId) {
        return getUserReputation(userId) >= MIN_REPUTATION_TO_EDIT;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDelete(Long userId) {
        return getUserReputation(userId) >= MIN_REPUTATION_TO_DELETE;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canModerate(Long userId) {
        return getUserReputation(userId) >= MIN_REPUTATION_TO_DELETE;
    }

    private ReputationHistoryDTO convertToDTO(ReputationHistory history) {
        return new ReputationHistoryDTO(
                history.getId(),
                history.getAction().name(),
                history.getPoints(),
                history.getDescription(),
                history.getCreatedAt(),
                history.getQuestionId(),
                history.getAnswerId()
        );
    }
}