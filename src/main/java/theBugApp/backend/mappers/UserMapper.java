package theBugApp.backend.mappers;

import org.springframework.stereotype.Component;
import theBugApp.backend.dto.InfoUserDto;
import theBugApp.backend.dto.UserDto;
import theBugApp.backend.entity.User;
import theBugApp.backend.repository.AnswerRepository;
import theBugApp.backend.repository.FollowRepository;
import theBugApp.backend.repository.QuestionRepository;
import theBugApp.backend.repository.VoteRepository;

@Component
public class UserMapper {

    private final FollowRepository followRepository;
    private final QuestionRepository questionRepository;
    private final VoteRepository voteRepository;
    private final AnswerRepository answerRepository;

    public UserMapper(FollowRepository followRepository,QuestionRepository questionRepository, VoteRepository voteRepository,AnswerRepository answerRepository) {
        this.followRepository = followRepository;
        this.questionRepository=questionRepository;
        this.voteRepository=voteRepository;
        this.answerRepository=answerRepository;
    }

    public UserDto toUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setUserId(user.getUserId());
        dto.setReputation(user.getReputation());
        dto.setPhotoUrl(user.getPhotoUrl());
        dto.setConfirmed(user.isConfirmed());
        // NOUVELLE LIGNE : Mapper le country
        dto.setCountry(user.getCountry());

        // Calculer les counts
        dto.setFollowersCount((int) followRepository.countFollowersByUserId(user.getUserId()));
        dto.setFollowingCount((int) followRepository.countFollowingByUserId(user.getUserId()));
        dto.setQuestionCount((int) questionRepository.countByUserId(user.getUserId()));
        dto.setVoteCount((int) voteRepository.countByUserId(user.getUserId()));
        dto.setAnswerCount((int) answerRepository.countByUserId(user.getUserId()));
        dto.setReachedCount(0);

        // Mapper les nouveaux champs
        dto.setCreatedDate(user.getCreatedDate());
        dto.setLastSeen(user.getLastSeen());
        dto.setGithubLink(user.getGithubLink());
        dto.setPortfolioLink(user.getPortfolioLink());
        dto.setAbout(user.getAbout());

        InfoUserDto infoDto = new InfoUserDto();
        infoDto.setUserId(user.getInfoUser().getUserId());
        infoDto.setUsername(user.getInfoUser().getUsername());
        infoDto.setEmail(user.getInfoUser().getEmail());

        dto.setInfoUser(infoDto);
        return dto;
    }

    // Méthode utilitaire pour mettre à jour un User depuis un DTO (optionnel)
    public void updateUserFromDto(User user, UserDto dto) {
        if (dto.getPhotoUrl() != null) {
            user.setPhotoUrl(dto.getPhotoUrl());
        }
        if (dto.getCountry() != null) {
            user.setCountry(dto.getCountry());
        }
        user.setConfirmed(dto.isConfirmed());
        if (dto.getReputation() != null) {
            user.setReputation(dto.getReputation());
        }
    }
}
