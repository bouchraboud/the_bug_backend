package theBugApp.backend.service;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import theBugApp.backend.dto.*;
import theBugApp.backend.entity.Question;
import theBugApp.backend.entity.Tag;
import theBugApp.backend.entity.User;
import theBugApp.backend.entity.Vote;
import theBugApp.backend.exception.QuestionNotFoundException;
import theBugApp.backend.exception.UnauthorizedActionException;
import theBugApp.backend.mappers.UserMapper;
import theBugApp.backend.repository.AnswerRepository;
import theBugApp.backend.repository.QuestionRepository;
import theBugApp.backend.repository.UserRepository;
import theBugApp.backend.repository.VoteRepository;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional  // Add @Transactional to ensure the entire operation completes
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final TagService tagService;
    private final VoteRepository voteRepository;
    private final NotificationService notificationService; // Add this dependency
    private final AnswerRepository answerRepository;
    private final AnswerService answerService;
    private final LexicalContentProcessor lexicalProcessor;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public QuestionResponseDTO createQuestion(QuestionRequestDTO request, String userEmail) {
        User user = userRepository.findByInfoUser_Email(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Question question = new Question();
        question.setTitle(request.title());
        question.setContent(request.content());
        String plainText = lexicalProcessor.extractPlainText(request.content());
        question.setPlainTextContent(plainText);
        question.setUser(user);

        if (request.tagNames() != null && !request.tagNames().isEmpty()) {
            Set<Tag> tags = tagService.getOrCreateTags(request.tagNames());
            question.setTags(new HashSet<>(tags));
        }

        Question savedQuestion = questionRepository.save(question);
        notificationService.notifyNewQuestionWithTags(savedQuestion);

        return convertToResponseDTO(savedQuestion);
    }
    @Override
    public QuestionResponseDTO getQuestionById(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException(id));
        return convertToResponseDTO(question);
    }

    @Transactional(readOnly = true)
    @Override
    public List<QuestionResponseDTO> getAllQuestions() {
        List<Question> questions = questionRepository.findAll();

        // Convert to DTOs immediately without trying to force initialization
        // We'll handle the lazy loading inside convertToResponseDTO
        return questions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public QuestionResponseDTO convertToResponseDTO(Question question) {
        // Calculate vote score
        int voteScore = 0;
        List<Vote> votes = voteRepository.findByQuestion(question);
        if (votes != null) {
            voteScore = votes.stream()
                    .mapToInt(v -> v.getVoteType() == Vote.VoteType.UPVOTE ? 1 : -1)
                    .sum();
        }

        // Use a new, unconnected collection to avoid Hibernate proxies
        Set<SimpleTagDTO> tagDTOs = new HashSet<>();

        // Only try to access tags if the proxy is already initialized
        // or safely retrieve it within the transaction
        try {
            if (org.hibernate.Hibernate.isInitialized(question.getTags())) {
                question.getTags().forEach(tag ->
                        tagDTOs.add(new SimpleTagDTO(tag.getName()))
                );
            } else {
                // Create DTOs directly from database without collection copy
                // This approach avoids the ConcurrentModificationException
                tagService.getTagsByQuestionId(question.getId())
                        .forEach(tagName -> tagDTOs.add(new SimpleTagDTO(tagName)));
            }
        } catch (Exception e) {
            // Fall back to empty set if there's any problem
            System.out.println("Error processing tags for question ID " + question.getId() + ": " + e.getMessage());
        }
        List<AnswerResponseDTO> answerDTOs = answerRepository.findByQuestionId(question.getId()).stream()
                .map(answerService::convertToDTO)
                .collect(Collectors.toList());
        UserDto userDto = null;
        if (question.getUser() != null) {
            userDto = userMapper.toUserDto(question.getUser());  // Call your mapping method here
        }
        return new QuestionResponseDTO(
                question.getId(),
                question.getTitle(),
                question.getContent(),
                question.getPlainTextContent(),
                question.getCreatedAt(),
                question.getUpdatedAt(),
                userDto,
                0, // viewCount
                voteScore,
                question.getAnswers().size(),
                tagDTOs,
                answerDTOs
        );
    }
    @Transactional(readOnly = true)
    @Override
    public List<QuestionResponseDTO> searchQuestions(String query, String tag, int page, int size) {
        List<Question> questions;

        // Apply filters for query and/or tag with pagination
        if (query != null && tag != null) {
            questions = questionRepository.searchByTitleOrContentAndTag(query, tag, PageRequest.of(page, size));
        } else if (query != null) {
            questions = questionRepository.searchQuestionsByTitleOrContent(query, PageRequest.of(page, size));
        } else if (tag != null) {
            questions = questionRepository.findByTags_Name(tag, PageRequest.of(page, size));
        } else {
            questions = Collections.emptyList();
        }

        // Convert to DTOs
        return questions.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // Add this method if you plan to implement question updates
    @Transactional
    @Override
    public QuestionResponseDTO updateQuestion(Long questionId, QuestionRequestDTO request, String userEmail) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        // Check if the user is the question owner
        if (!question.getUser().getInfoUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedActionException("Only the question owner can update the question");
        }

        question.setTitle(request.title());
        question.setContent(request.content());

        if (request.tagNames() != null && !request.tagNames().isEmpty()) {
            Set<Tag> tags = tagService.getOrCreateTags(request.tagNames());
            question.setTags(new HashSet<>(tags));
        }

        Question updatedQuestion = questionRepository.save(question);

        // Send notifications about question update
        notificationService.notifyQuestionUpdate(updatedQuestion);

        return convertToResponseDTO(updatedQuestion);
    }
}