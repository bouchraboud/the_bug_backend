package theBugApp.backend.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import theBugApp.backend.dto.UserDto;
import theBugApp.backend.entity.*;
import theBugApp.backend.exception.*;
import theBugApp.backend.mappers.UserMapper;
import theBugApp.backend.repository.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private final TagRepository tagRepository;
    private final FollowTagRepository followTagRepository;
    private final QuestionRepository questionRepository;
    private final FollowQuestionRepository followQuestionRepository;
    private final AnswerRepository answerRepository;
    private final FollowAnswerRepository followAnswerRepository;

    // Suivre un utilisateur
    public boolean followUser(Long followerId, Long followingId) throws UserNotFoundException {
        // Vérifier que les utilisateurs existent
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new UserNotFoundException("Follower not found"));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new UserNotFoundException("User to follow not found"));

        // On ne peut pas se suivre soi-mêmebb-
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }

        // Vérifier si la relation existe déjà
        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            return false; // Déjà suivi
        }

        // Créer la relation de suivi
        Follow follow = new Follow(follower, following);
        followRepository.save(follow);
        return true;
    }

    // Ne plus suivre un utilisateurs
    public boolean unfollowUser(Long followerId, Long followingId) throws UserNotFoundException {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new UserNotFoundException("Follower not found"));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new UserNotFoundException("User to unfollow not found"));

        // Vérifier si la relation existe
        if (!followRepository.existsByFollowerAndFollowing(follower, following)) {
            return false; // Pas encore suivi
        }

        // Supprimer la relation
        followRepository.deleteByFollowerAndFollowing(follower, following);
        return true;
    }

    // Vérifier si user1 suit user2
    public boolean isFollowing(Long followerId, Long followingId) throws UserNotFoundException {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new UserNotFoundException("Follower not found"));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return followRepository.existsByFollowerAndFollowing(follower, following);
    }

    // Obtenir les followers d'un utilisateur
    @Transactional(readOnly = true)
    public List<UserDto> getFollowers(Long userId) throws UserNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return followRepository.findByFollowing(user)
                .stream()
                .map(follow -> userMapper.toUserDto(follow.getFollower()))
                .collect(Collectors.toList());
    }

    // Obtenir les utilisateurs qu'un utilisateur suit
    @Transactional(readOnly = true)
    public List<UserDto> getFollowing(Long userId) throws UserNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return followRepository.findByFollower(user)
                .stream()
                .map(follow -> userMapper.toUserDto(follow.getFollowing()))
                .collect(Collectors.toList());
    }

    // Obtenir le nombre de followers
    public long getFollowersCount(Long userId) {
        return followRepository.countFollowersByUserId(userId);
    }

    // Obtenir le nombre de following
    public long getFollowingCount(Long userId) {
        return followRepository.countFollowingByUserId(userId);
    }

    public void followTag(Long tagId, String userEmail) {
        User user = userRepository.findByInfoUser_Email(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new TagNotFoundException("Tag not found"));

        FollowTag followTag = new FollowTag();
        followTag.setUser(user);
        followTag.setTag(tag);

        followTagRepository.save(followTag);
    }

    public void followQuestion(Long questionId, String userEmail) {
        User user = userRepository.findByInfoUser_Email(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException(questionId));
        if (question.getUser().getUserId().equals(user.getUserId())) {
            throw new UnauthorizedActionException("You cannot follow your own question");
        }
        FollowQuestion followQuestion = new FollowQuestion();
        followQuestion.setUser(user);
        followQuestion.setQuestion(question);
        followQuestionRepository.save(followQuestion);
    }
    public void followAnswer(Long answerId, String userEmail) {
        User user = userRepository.findByInfoUser_Email(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new AnswerNotFoundException(answerId));
        if (answer.getUser().getUserId().equals(user.getUserId())) {
            throw new UnauthorizedActionException("You cannot follow your own answer");
        }
        FollowAnswer followAnswer = new FollowAnswer();
        followAnswer.setUser(user);
        followAnswer.setAnswer(answer);
        followAnswerRepository.save(followAnswer);
    }

    public void unfollowTag(Long tagId, String userEmail) {
        User user = userRepository.findByInfoUser_Email(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new TagNotFoundException("Tag not found"));

        FollowTag followTag = followTagRepository.findByUserAndTag(user, tag)
                .orElseThrow(() -> new RuntimeException("Follow tag relationship not found"));

        followTagRepository.delete(followTag);
    }

    public void unfollowQuestion(Long questionId, String userEmail) {
        User user = userRepository.findByInfoUser_Email(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException(questionId));

        FollowQuestion followQuestion = followQuestionRepository.findByUserAndQuestion(user, question)
                .orElseThrow(() -> new FollowNotFoundException("User is not following this question"));

        followQuestionRepository.delete(followQuestion);
    }


    public void unfollowAnswer(Long answerId, String userEmail) {
        User user = userRepository.findByInfoUser_Email(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new AnswerNotFoundException(answerId));

        FollowAnswer followAnswer = followAnswerRepository.findByUserAndAnswer(user, answer)
                .orElseThrow(() -> new RuntimeException("Follow answer relationship not found"));

        followAnswerRepository.delete(followAnswer);
    }


}