package theBugApp.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import theBugApp.backend.entity.*;
import theBugApp.backend.exception.NotFoundException;
import theBugApp.backend.repository.AnswerRepository;
import theBugApp.backend.repository.QuestionRepository;
import theBugApp.backend.repository.UserRepository;
import theBugApp.backend.repository.VoteRepository;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VoteServiceImpl implements VoteService {

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

        Optional<Vote> existingVote = voteRepository.findByUserAndQuestion(user, question);

        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();
            if (vote.getVoteType() == voteType) {
                // Same vote type - remove the vote
                voteRepository.delete(vote);
                return 0;
            } else {
                // Different vote type - update the vote
                vote.setVoteType(voteType);
                voteRepository.save(vote);
                return voteType == Vote.VoteType.UPVOTE ? 1 : -1;
            }
        } else {
            // New vote
            Vote newVote = new Vote();
            newVote.setUser(user);
            newVote.setQuestion(question);
            newVote.setVoteType(voteType);
            voteRepository.save(newVote);
            return voteType == Vote.VoteType.UPVOTE ? 1 : -1;
        }
    }

    @Override
    @Transactional
    public int voteAnswer(Long answerId, String userEmail, Vote.VoteType voteType) {
        User user = userRepository.findByInfoUser_Email(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new NotFoundException("answer not found"));

        Optional<Vote> existingVote = voteRepository.findByUserAndAnswer(user, answer);

        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();
            if (vote.getVoteType() == voteType) {
                // Same vote type - remove the vote
                voteRepository.delete(vote);
                return 0;
            } else {
                // Different vote type - update the vote
                vote.setVoteType(voteType);
                voteRepository.save(vote);
                return voteType == Vote.VoteType.UPVOTE ? 1 : -1;
            }
        } else {
            // New vote
            Vote newVote = new Vote();
            newVote.setUser(user);
            newVote.setAnswer(answer);
            newVote.setVoteType(voteType);
            voteRepository.save(newVote);
            return voteType == Vote.VoteType.UPVOTE ? 1 : -1;
        }
    }
}