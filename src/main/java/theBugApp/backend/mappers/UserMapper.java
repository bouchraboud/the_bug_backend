package theBugApp.backend.mappers;

import org.springframework.stereotype.Component;
import theBugApp.backend.dto.InfoUserDto;
import theBugApp.backend.dto.UserDto;
import theBugApp.backend.entity.User;

@Component
public class UserMapper {

    public UserDto toUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setUserId(user.getUserId());
        dto.setReputation(user.getReputation());
        dto.setPhotoUrl(user.getPhotoUrl());
        dto.setConfirmed(user.isConfirmed());

        InfoUserDto infoDto = new InfoUserDto();
        infoDto.setUserId(user.getInfoUser().getUserId());
        infoDto.setUsername(user.getInfoUser().getUsername());
        infoDto.setEmail(user.getInfoUser().getEmail());

        dto.setInfoUser(infoDto);
        return dto;
    }
}
