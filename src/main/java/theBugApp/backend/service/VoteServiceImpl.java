package theBugApp.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import theBugApp.backend.dto.VoteInfoDto;
import theBugApp.backend.entity.*;
import theBugApp.backend.enums.ReputationAction;
import theBugApp.backend.exception.InsufficientReputationException;
import theBugApp.backend.exception.NotFoundException;
import theBugApp.backend.repository.AnswerRepository;
import theBugApp.backend.repository.QuestionRepository;
import theBugApp.backend.repository.UserRepository;
import theBugApp.backend.repository.VoteRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoteServiceImpl implements VoteService {

    @Autowired
    private ReputationService reputationService;

    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    @Override
    @Transactional
    public int voteQuestion(Long questionId, String userEmail, Vote.VoteType voteType) {
        User user = userRepository.findByInfoUser_Email(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found"));

        // Check if user has permission to vote
        if (voteType == Vote.VoteType.UPVOTE && !reputationService.canUpvote(user.getUserId())) {
            throw new InsufficientReputationException("You need at least 15 reputation to upvote");
        }
        if (voteType == Vote.VoteType.DOWNVOTE && !reputationService.canDownvote(user.getUserId())) {
            throw new InsufficientReputationException("You need at least 125 reputation to downvote");
        }

        // Prevent users from voting on their own questions
        if (question.getUser().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("You cannot vote on your own question");
        }

        Optional<Vote> existingVote = voteRepository.findByUserAndQuestion(user, question);

        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();
            if (vote.getVoteType() == voteType) {
                // Same vote type - remove the vote and reverse reputation
                reverseQuestionVoteReputation(vote, question.getUser().getUserId(), user.getUserId());
                voteRepository.delete(vote);
                return 0;
            } else {
                // Different vote type - update the vote and adjust reputation
                Vote.VoteType oldVoteType = vote.getVoteType();
                vote.setVoteType(voteType);
                voteRepository.save(vote);

                // Reverse old reputation
                reverseQuestionVoteReputation(vote, question.getUser().getUserId(), user.getUserId(), oldVoteType);
                // Award new reputation
                awardQuestionVoteReputation(vote, question.getUser().getUserId(), user.getUserId(), voteType);

                return voteType == Vote.VoteType.UPVOTE ? 1 : -1;
            }
        } else {
            // New vote
            Vote newVote = new Vote();
            newVote.setUser(user);
            newVote.setQuestion(question);
            newVote.setVoteType(voteType);
            Vote savedVote = voteRepository.save(newVote);

            // Award reputation
            awardQuestionVoteReputation(savedVote, question.getUser().getUserId(), user.getUserId(), voteType);

            return voteType == Vote.VoteType.UPVOTE ? 1 : -1;
        }
    }

    @Override
    @Transactional
    public int voteAnswer(Long answerId, String userEmail, Vote.VoteType voteType) {
        User user = userRepository.findByInfoUser_Email(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new NotFoundException("Answer not found"));

        // Check if user has permission to vote
        if (voteType == Vote.VoteType.UPVOTE && !reputationService.canUpvote(user.getUserId())) {
            throw new InsufficientReputationException("You need at least 15 reputation to upvote");
        }
        if (voteType == Vote.VoteType.DOWNVOTE && !reputationService.canDownvote(user.getUserId())) {
            throw new InsufficientReputationException("You need at least 125 reputation to downvote");
        }

        // Prevent users from voting on their own answers
        if (answer.getUser().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("You cannot vote on your own answer");
        }

        Optional<Vote> existingVote = voteRepository.findByUserAndAnswer(user, answer);

        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();
            if (vote.getVoteType() == voteType) {
                // Same vote type - remove the vote and reverse reputation
                reverseAnswerVoteReputation(vote, answer.getUser().getUserId(), user.getUserId());
                voteRepository.delete(vote);
                return 0;
            } else {
                // Different vote type - update the vote and adjust reputation
                Vote.VoteType oldVoteType = vote.getVoteType();
                vote.setVoteType(voteType);
                voteRepository.save(vote);

                // Reverse old reputation
                reverseAnswerVoteReputation(vote, answer.getUser().getUserId(), user.getUserId(), oldVoteType);
                // Award new reputation
                awardAnswerVoteReputation(vote, answer.getUser().getUserId(), user.getUserId(), voteType);

                return voteType == Vote.VoteType.UPVOTE ? 1 : -1;
            }
        } else {
            // New vote
            Vote newVote = new Vote();
            newVote.setUser(user);
            newVote.setAnswer(answer);
            newVote.setVoteType(voteType);
            Vote savedVote = voteRepository.save(newVote);

            // Award reputation
            awardAnswerVoteReputation(savedVote, answer.getUser().getUserId(), user.getUserId(), voteType);

            return voteType == Vote.VoteType.UPVOTE ? 1 : -1;
        }
    }

    // Helper methods for reputation management
    private void awardQuestionVoteReputation(Vote vote, Long questionAuthorId, Long voterId, Vote.VoteType voteType) {
        if (voteType == Vote.VoteType.UPVOTE) {
            // Award reputation to question author
            reputationService.awardReputation(questionAuthorId, ReputationAction.QUESTION_UPVOTE,
                    vote.getQuestion().getId(), null, vote.getId());
        } else {
            // Downvote: penalize question author and voter
            reputationService.awardReputation(questionAuthorId, ReputationAction.QUESTION_DOWNVOTE,
                    vote.getQuestion().getId(), null, vote.getId());
            reputationService.awardReputation(voterId, ReputationAction.DOWNVOTE_GIVEN);
        }
    }

    private void awardAnswerVoteReputation(Vote vote, Long answerAuthorId, Long voterId, Vote.VoteType voteType) {
        if (voteType == Vote.VoteType.UPVOTE) {
            // Award reputation to answer author
            reputationService.awardReputation(answerAuthorId, ReputationAction.ANSWER_UPVOTE,
                    vote.getAnswer().getQuestion().getId(), vote.getAnswer().getId(), vote.getId());
        } else {
            // Downvote: penalize answer author and voter
            reputationService.awardReputation(answerAuthorId, ReputationAction.ANSWER_DOWNVOTE,
                    vote.getAnswer().getQuestion().getId(), vote.getAnswer().getId(), vote.getId());
            reputationService.awardReputation(voterId, ReputationAction.DOWNVOTE_GIVEN);
        }
    }

    private void reverseQuestionVoteReputation(Vote vote, Long questionAuthorId, Long voterId) {
        reverseQuestionVoteReputation(vote, questionAuthorId, voterId, vote.getVoteType());
    }

    private void reverseQuestionVoteReputation(Vote vote, Long questionAuthorId, Long voterId, Vote.VoteType voteType) {
        if (voteType == Vote.VoteType.UPVOTE) {
            // Reverse upvote reputation (subtract points)
            reputationService.awardCustomReputation(questionAuthorId, -5, "Question upvote removed",
                    vote.getQuestion().getId(), null);
        } else {
            // Reverse downvote reputation (add back points)
            reputationService.awardCustomReputation(questionAuthorId, 2, "Question downvote removed",
                    vote.getQuestion().getId(), null);
            reputationService.awardCustomReputation(voterId, 1, "Downvote penalty removed", null, null);
        }
    }

    private void reverseAnswerVoteReputation(Vote vote, Long answerAuthorId, Long voterId) {
        reverseAnswerVoteReputation(vote, answerAuthorId, voterId, vote.getVoteType());
    }

    private void reverseAnswerVoteReputation(Vote vote, Long answerAuthorId, Long voterId, Vote.VoteType voteType) {
        if (voteType == Vote.VoteType.UPVOTE) {
            // Reverse upvote reputation (subtract points)
            reputationService.awardCustomReputation(answerAuthorId, -10, "Answer upvote removed",
                    vote.getAnswer().getQuestion().getId(), vote.getAnswer().getId());
        } else {
            // Reverse downvote reputation (add back points)
            reputationService.awardCustomReputation(answerAuthorId, 2, "Answer downvote removed",
                    vote.getAnswer().getQuestion().getId(), vote.getAnswer().getId());
            reputationService.awardCustomReputation(voterId, 1, "Downvote penalty removed", null, null);
        }
    }

    public List<VoteInfoDto> getVoteInfoByQuestion(Long questionId) {
        List<Vote> votes = voteRepository.findVotesByQuestionId(questionId);
        return votes.stream()
                .map(vote -> new VoteInfoDto(vote.getUser().getUserId(), vote.getVoteType()))
                .collect(Collectors.toList());
    }
    public List<VoteInfoDto> getVoteInfoByAnswer(Long answerId) {
        List<Vote> votes = voteRepository.findVotesByAnswerId(answerId);
        return votes.stream()
                .map(vote -> new VoteInfoDto(vote.getUser().getUserId(), vote.getVoteType()))
                .collect(Collectors.toList());
    }
}