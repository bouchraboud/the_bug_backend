package theBugApp.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import theBugApp.backend.dto.AnswerResponseDTO;
import theBugApp.backend.dto.FullTagDTO;
import theBugApp.backend.dto.QuestionResponseDTO;
import theBugApp.backend.dto.SimpleTagDTO;
import theBugApp.backend.entity.Question;
import theBugApp.backend.entity.Tag;
import theBugApp.backend.repository.AnswerRepository;
import theBugApp.backend.repository.QuestionRepository;
import theBugApp.backend.repository.TagRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TagService {
    private final TagRepository tagRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final AnswerService answerService;
    public TagService(TagRepository tagRepository, QuestionRepository questionRepository, AnswerRepository answerRepository,AnswerService answerService) {
        this.tagRepository = tagRepository;
        this.questionRepository = questionRepository;
        this.answerRepository=answerRepository;
        this.answerService=answerService;
    }

    @Transactional
    public Set<Tag> getOrCreateTags(Set<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return Collections.emptySet();
        }

        // Find existing tags in one query
        List<Tag> existingTags = tagRepository.findByNameIn(tagNames);

        // Create new tags for names that don't exist
        Set<String> existingTagNames = existingTags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        Set<Tag> newTags = tagNames.stream()
                .filter(name -> !existingTagNames.contains(name))
                .map(name -> {
                    Tag tag = new Tag(name);
                    return tagRepository.save(tag);
                })
                .collect(Collectors.toSet());

        // Combine existing and new tags
        Set<Tag> allTags = new HashSet<>(existingTags);
        allTags.addAll(newTags);

        return allTags;
    }

    public List<SimpleTagDTO> getAllTags() {
        return tagRepository.findAll().stream()
                .map(tag -> new SimpleTagDTO(tag.getName()))
                .collect(Collectors.toList());
    }

    public List<FullTagDTO> getPopularTags() {
        return tagRepository.findAll().stream()
                .map(tag -> new FullTagDTO(
                        tag.getId(),
                        tag.getName(),
                        tag.getQuestions().size()))
                .sorted((t1, t2) -> Integer.compare(t2.usageCount(), t1.usageCount()))
                .limit(20)
                .collect(Collectors.toList());
    }

    public List<QuestionResponseDTO> getQuestionsByTagName(String tagName) {
        return tagRepository.findByName(tagName.toLowerCase().trim())
                .map(tag -> questionRepository.findByTagName(tagName).stream()
                        .map(this::convertQuestionToDTO)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    public Set<String> getTagsByQuestionId(Long questionId) {
        return tagRepository.findTagNamesByQuestionId(questionId);
    }

    public Map<Long, Set<String>> getTagsForQuestions(List<Long> questionIds) {
        List<Object[]> results = tagRepository.findTagNamesForQuestionIds(questionIds);
        Map<Long, Set<String>> tagsMap = new HashMap<>();

        for (Object[] result : results) {
            Long questionId = (Long) result[0];
            String tagName = (String) result[1];
            tagsMap.computeIfAbsent(questionId, k -> new HashSet<>()).add(tagName);
        }

        return tagsMap;
    }

    private QuestionResponseDTO convertQuestionToDTO(Question question) {
        Set<String> tagNames = getTagsByQuestionId(question.getId());
        Set<SimpleTagDTO> tagDTOs = tagNames.stream()
                .map(SimpleTagDTO::new)
                .collect(Collectors.toSet());
        List<AnswerResponseDTO> answerDTOs = answerRepository.findByQuestionId(question.getId()).stream()
                .map(answerService::convertToDTO)
                .collect(Collectors.toList());

        return new QuestionResponseDTO(
                question.getId(),
                question.getTitle(),
                question.getContent(),
                question.getCreatedAt(),
                question.getUpdatedAt(),
                question.getUser().getInfoUser().getUsername(),
                question.getUser().getInfoUser().getEmail(),
                0, // viewCount
                0, // voteScore
                question.getAnswers().size(),
                tagDTOs,
                answerDTOs
        );
    }
}