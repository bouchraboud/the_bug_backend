package theBugApp.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import theBugApp.backend.dto.QuestionResponseDTO;
import theBugApp.backend.dto.QuestionRequestDTO;
import theBugApp.backend.dto.SimpleTagDTO;
import theBugApp.backend.entity.Question;
import theBugApp.backend.entity.Tag;
import theBugApp.backend.entity.User;
import theBugApp.backend.entity.Vote;
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


    @Override
    public QuestionResponseDTO createQuestion(QuestionRequestDTO request, String userEmail) {
        User user = userRepository.findByInfoUser_Email(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Question question = new Question();
        question.setTitle(request.title());
        question.setContent(request.content());
        question.setUser(user);

        if (request.tagNames() != null && !request.tagNames().isEmpty()) {
            Set<Tag> tags = tagService.getOrCreateTags(request.tagNames());
            // Create a new HashSet to avoid potential Hibernate proxy issues
            question.setTags(new HashSet<>(tags));
        }

        Question savedQuestion = questionRepository.save(question);
        return convertToResponseDTO(savedQuestion);
    }

    @Override
    public QuestionResponseDTO getQuestionById(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));
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

        return new QuestionResponseDTO(
                question.getId(),
                question.getTitle(),
                question.getContent(),
                question.getCreatedAt(),
                question.getUpdatedAt(),
                question.getUser().getInfoUser().getUsername(),
                question.getUser().getInfoUser().getEmail(),
                0, // viewCount
                voteScore, // Now includes actual vote score
                question.getAnswers().size(),
                tagDTOs
        );
    }
}