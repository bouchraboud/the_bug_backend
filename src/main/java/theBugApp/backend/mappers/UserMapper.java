package theBugApp.backend.mappers;

import org.springframework.stereotype.Component;
import theBugApp.backend.dto.InfoUserDto;
import theBugApp.backend.dto.UserDto;
import theBugApp.backend.entity.User;
import theBugApp.backend.repository.FollowRepository;

@Component
public class UserMapper {

    private final FollowRepository followRepository;

    public UserMapper(FollowRepository followRepository) {
        this.followRepository = followRepository;
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
