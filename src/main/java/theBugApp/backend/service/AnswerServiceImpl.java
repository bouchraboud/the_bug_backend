package theBugApp.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import theBugApp.backend.dto.AnswerRequestDTO;
import theBugApp.backend.dto.AnswerResponseDTO;
import theBugApp.backend.entity.Answer;
import theBugApp.backend.entity.Question;
import theBugApp.backend.entity.User;
import theBugApp.backend.entity.Vote;
import theBugApp.backend.enums.ReputationAction;
import theBugApp.backend.exception.AnswerNotFoundException;
import theBugApp.backend.exception.InsufficientReputationException;
import theBugApp.backend.exception.QuestionNotFoundException;
import theBugApp.backend.exception.UnauthorizedActionException;
import theBugApp.backend.exception.UserNotFoundException;
import theBugApp.backend.repository.AnswerRepository;
import theBugApp.backend.repository.QuestionRepository;
import theBugApp.backend.repository.UserRepository;
import theBugApp.backend.repository.VoteRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final VoteRepository voteRepository;
    private final NotificationService notificationService;

    @Autowired
    private ReputationService reputationService;

    @Override
    @Transactional
    public AnswerResponseDTO createAnswer(AnswerRequestDTO answerRequest, String userEmail) {
        User user = userRepository.findByInfoUser_Email(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

        // Find question or throw QuestionNotFoundException
        Question question = questionRepository.findById(answerRequest.questionId())
                .orElseThrow(() -> new QuestionNotFoundException(answerRequest.questionId()));

        // Check if user has permission to answer (optional: you might want a minimum reputation)
        // Uncomment if you want to require reputation to answer
        // if (!reputationService.canComment(user.getUserId())) {
        //     throw new InsufficientReputationException("You need at least 50 reputation to answer");
        // }

        Answer answer = new Answer();
        answer.setContent(answerRequest.content());
        answer.setUser(user);
        answer.setQuestion(question);

        Answer savedAnswer = answerRepository.save(answer);

        // Add the answer to the question's collection
        question.getAnswers().add(savedAnswer);
        questionRepository.save(question);

        // Add the answer to the user's collection
        user.getAnswers().add(savedAnswer);
        userRepository.save(user);

        // Award reputation for asking a question (if you want to give points for participation)
        // reputationService.awardReputation(user.getUserId(), ReputationAction.QUESTION_ASKED,
        //     question.getId(), savedAnswer.getId(), null);

        // Send notifications to question followers
        notificationService.notifyNewAnswer(question.getId(), savedAnswer);

        return convertToDTO(savedAnswer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnswerResponseDTO> getAnswersByQuestionId(Long questionId) {
        return answerRepository.findByQuestionId(questionId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnswerResponseDTO> getAnswersByUserId(Long userId) {
        return answerRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private AnswerResponseDTO convertToDTO(Answer answer) {
        List<Vote> votes = voteRepository.findByAnswer(answer);
        int voteScore = (votes != null)
                ? votes.stream()
                .mapToInt(v -> v.getVoteType() == Vote.VoteType.UPVOTE ? 1 : -1)
                .sum()
                : 0;

        return new AnswerResponseDTO(
                answer.getId(),
                answer.getContent(),
                answer.getCreatedAt(),
                answer.getUpdatedAt(),
                answer.isAccepted(),
                voteScore,
                answer.getUser().getInfoUser().getUsername(),
                answer.getUser().getInfoUser().getEmail(),
                answer.getQuestion().getId()
        );
    }

    @Override
    @Transactional
    public AnswerResponseDTO acceptAnswer(Long answerId, String userEmail) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new AnswerNotFoundException(answerId));

        Question question = answer.getQuestion();
        User questionOwner = question.getUser();

        if (!questionOwner.getInfoUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedActionException("Only the question owner can accept an answer");
        }

        // Check if there's already an accepted answer for this question
        List<Answer> existingAcceptedAnswers = answerRepository.findByQuestionId(question.getId())
                .stream()
                .filter(Answer::isAccepted)
                .collect(Collectors.toList());

        // If there's already an accepted answer, unaccept it first
        if (!existingAcceptedAnswers.isEmpty()) {
            for (Answer acceptedAnswer : existingAcceptedAnswers) {
                acceptedAnswer.setAccepted(false);
                answerRepository.save(acceptedAnswer);

                // Reverse the acceptance reputation
                reputationService.awardReputation(acceptedAnswer.getUser().getUserId(),
                        ReputationAction.ANSWER_UNACCEPTED,
                        question.getId(),
                        acceptedAnswer.getId(),
                        null);
            }
        }

        // Accept the new answer
        answer.setAccepted(true);
        Answer updatedAnswer = answerRepository.save(answer);

        // Award reputation for answer acceptance
        reputationService.awardReputation(answer.getUser().getUserId(),
                ReputationAction.ANSWER_ACCEPTED,
                question.getId(),
                answerId,
                null);

        // Send notifications about answer acceptance
        notificationService.notifyAnswerAccepted(updatedAnswer);

        return convertToDTO(updatedAnswer);
    }

    @Override
    @Transactional
    public AnswerResponseDTO disacceptAnswer(Long answerId, String userEmail) {
        // Retrieve the answer
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new AnswerNotFoundException(answerId));

        // Retrieve the question associated with the answer
        Question question = answer.getQuestion();
        User questionOwner = question.getUser();

        // Check if the user is the question owner
        if (!questionOwner.getInfoUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedActionException("Only the question owner can disaccept an answer");
        }

        // Check if the answer is currently accepted
        if (!answer.isAccepted()) {
            throw new IllegalStateException("This answer is not currently accepted");
        }

        // Set 'isAccepted' to false
        answer.setAccepted(false);
        Answer updatedAnswer = answerRepository.save(answer);

        // Remove reputation for answer acceptance
        reputationService.awardReputation(answer.getUser().getUserId(),
                ReputationAction.ANSWER_UNACCEPTED,
                question.getId(),
                answerId,
                null);

        // Convert the updated answer to a DTO
        return convertToDTO(updatedAnswer);
    }

    @Transactional
    @Override
    public AnswerResponseDTO updateAnswer(Long answerId, AnswerRequestDTO answerRequest, String userEmail) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new AnswerNotFoundException(answerId));

        // Check if the user is the answer owner
        if (!answer.getUser().getInfoUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedActionException("Only the answer owner can update the answer");
        }

        // Check if user has permission to edit (optional privilege check)
        User user = userRepository.findByInfoUser_Email(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

        // If the user is not the owner, check edit privileges
        if (!answer.getUser().getUserId().equals(user.getUserId()) &&
                !reputationService.canEdit(user.getUserId())) {
            throw new InsufficientReputationException("You need at least 2000 reputation to edit others' answers");
        }

        answer.setContent(answerRequest.content());
        Answer updatedAnswer = answerRepository.save(answer);

        // Send notifications about answer update
        notificationService.notifyAnswerUpdate(updatedAnswer);

        return convertToDTO(updatedAnswer);
    }

    // Additional method to delete answer (with reputation checks)
    @Transactional
    public void deleteAnswer(Long answerId, String userEmail) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new AnswerNotFoundException(answerId));

        User user = userRepository.findByInfoUser_Email(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));

        // Check if user is the owner or has delete privileges
        boolean isOwner = answer.getUser().getUserId().equals(user.getUserId());
        boolean canDelete = reputationService.canDelete(user.getUserId());

        if (!isOwner && !canDelete) {
            throw new UnauthorizedActionException("You don't have permission to delete this answer");
        }

        // If answer was accepted, reverse the reputation
        if (answer.isAccepted()) {
            reputationService.awardReputation(answer.getUser().getUserId(),
                    ReputationAction.ANSWER_UNACCEPTED,
                    answer.getQuestion().getId(),
                    answerId,
                    null);
        }

        // Remove votes and their associated reputation (this would require additional logic)
        // You might want to implement this to properly handle reputation when deleting

        answerRepository.delete(answer);
    }
}