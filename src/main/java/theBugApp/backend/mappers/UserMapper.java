package theBugApp.backend.mappers;

import org.springframework.stereotype.Service;
import theBugApp.backend.dto.UserDto;
import theBugApp.backend.entity.User;

@Service
public class UserMapper {
    public UserDto toUserDto(User user) {

        UserDto userDto = new UserDto();
        userDto.setUserId(user.getUserId());
        userDto.setInfoUser(user.getInfoUser());
        userDto.setReputation(user.getReputation());
        userDto.setPhotoUrl(user.getPhotoUrl());
        userDto.setConfirmed(user.isConfirmed());

        return userDto;
    }
}
