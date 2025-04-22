package theBugApp.backend.dto;
import lombok.Data;
import theBugApp.backend.entity.InfoUser;

@Data
public class UserDto {
    private Long userId; // C’est ça que tu dois garder
    private InfoUser infoUser;
    private Integer reputation;
    private String photoUrl;
    private boolean isConfirmed;
}