package theBugApp.backend.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import theBugApp.backend.dto.UserDto;
import theBugApp.backend.entity.Follow;
import theBugApp.backend.entity.User;
import theBugApp.backend.exception.UserNotFoundException;
import theBugApp.backend.mappers.UserMapper;
import theBugApp.backend.repository.FollowRepository;
import theBugApp.backend.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

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
}