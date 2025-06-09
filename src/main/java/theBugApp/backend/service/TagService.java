package theBugApp.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import theBugApp.backend.dto.*;
import theBugApp.backend.entity.Question;
import theBugApp.backend.entity.Tag;
import theBugApp.backend.entity.Vote;
import theBugApp.backend.exception.TagNotFoundException;
import theBugApp.backend.mappers.UserMapper;
import theBugApp.backend.repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TagService {
    private final TagRepository tagRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final AnswerService answerService;
    private final FollowTagRepository followTagRepository;
    private final UserMapper userMapper;
    private final VoteRepository voteRepository;

    public TagService(TagRepository tagRepository, QuestionRepository questionRepository, AnswerRepository answerRepository, AnswerService answerService, FollowTagRepository followTagRepository, UserMapper userMapper, VoteRepository voteRepository) {
        this.tagRepository = tagRepository;
        this.questionRepository = questionRepository;
        this.answerRepository=answerRepository;
        this.answerService=answerService;
        this.followTagRepository = followTagRepository;
        this.userMapper = userMapper;
        this.voteRepository = voteRepository;
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

    public List<FullTagDTO> getAllTags() {
        return tagRepository.findAll().stream()
                .map(tag -> {
                    List<UserDto> followersDtos = followTagRepository.findFollowersByTagId(tag.getId()).stream()
                            .map(userMapper::toUserDto)
                            .collect(Collectors.toList());

                    int followersCount = followersDtos.size();

                    return new FullTagDTO(
                            tag.getId(),
                            tag.getName(),
                            tag.getQuestions().size(),
                            followersCount
                    );
                })
                .sorted((t1, t2) -> Integer.compare(t2.usageCount(), t1.usageCount()))
                .limit(20)
                .collect(Collectors.toList());
    }




    public List<FullTagDTO> getPopularTags() {
        return tagRepository.findAll().stream()
                .map(tag -> {
                    List<UserDto> followersDtos = followTagRepository.findFollowersByTagId(tag.getId()).stream()
                            .map(userMapper::toUserDto)
                            .collect(Collectors.toList());

                    int followersCount = followersDtos.size();



                    return new FullTagDTO(
                            tag.getId(),
                            tag.getName(),
                            tag.getQuestions().size(),
                            followersCount

                    );
                })
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

    QuestionResponseDTO convertQuestionToDTO(Question question) {
        int voteScore = 0;
        List<Vote> votes = voteRepository.findByQuestion(question);
        if (votes != null) {
            voteScore = votes.stream()
                    .mapToInt(v -> v.getVoteType() == Vote.VoteType.UPVOTE ? 1 : -1)
                    .sum();
        }
        Set<String> tagNames = getTagsByQuestionId(question.getId());
        Set<SimpleTagDTO> tagDTOs = tagNames.stream()
                .map(SimpleTagDTO::new)
                .collect(Collectors.toSet());

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
                tagDTOs
        );
    }

    public FullTagDTO getTagByName(String tagName) {
        return tagRepository.findByName(tagName.toLowerCase().trim())
                .map(tag -> {
                    List<UserDto> followersDtos = followTagRepository.findFollowersByTagId(tag.getId()).stream()
                            .map(userMapper::toUserDto)
                            .toList();

                    int followersCount = followersDtos.size();

                    return new FullTagDTO(
                            tag.getId(),
                            tag.getName(),
                            tag.getQuestions().size(),
                            followersCount
                    );
                })
                .orElseThrow(() -> new TagNotFoundException("Tag not found with name: " + tagName));
    }

}