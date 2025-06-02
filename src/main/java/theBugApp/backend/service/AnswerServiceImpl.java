package theBugApp.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import theBugApp.backend.dto.AnswerRequestDTO;
import theBugApp.backend.dto.AnswerResponseDTO;
import theBugApp.backend.entity.Answer;
import theBugApp.backend.entity.Question;
import theBugApp.backend.entity.User;
import theBugApp.backend.entity.Vote;
import theBugApp.backend.exception.AnswerNotFoundException;
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

    @Override
    @Transactional
    public AnswerResponseDTO createAnswer(AnswerRequestDTO answerRequest, String userEmail) {
        User user = userRepository.findByInfoUser_Email(userEmail)
                .orElseThrow(() -> new UserNotFoundException(userEmail));
        // Find question or throw QuestionNotFoundException
        Question question = questionRepository.findById(answerRequest.questionId())
                .orElseThrow(() -> new QuestionNotFoundException(answerRequest.questionId()));

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
    public AnswerResponseDTO acceptAnswer(Long answerId, String userEmail) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new AnswerNotFoundException(answerId));

        Question question = answer.getQuestion();
        User questionOwner = question.getUser();

        if (!questionOwner.getInfoUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedActionException("Only the question owner can accept an answer");
        }

        answer.setAccepted(true);
        Answer updatedAnswer = answerRepository.save(answer);

        return convertToDTO(updatedAnswer);
    }

    @Override
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

        // Set 'isAccepted' to false
        answer.setAccepted(false);
        Answer updatedAnswer = answerRepository.save(answer);

        // Convert the updated answer to a DTO
        return convertToDTO(updatedAnswer);
    }



}