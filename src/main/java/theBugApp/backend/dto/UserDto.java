package theBugApp.backend.dto;
import lombok.Data;
import theBugApp.backend.entity.InfoUser;
import theBugApp.backend.enums.Country;

@Data
public class UserDto {
    private Long userId; // C’est ça que tu dois garder
    private InfoUserDto infoUser;
    private Integer reputation;
    private String photoUrl;
    private boolean isConfirmed;
    // Nouveaux champs pour les statistiques de suivi
    private int followersCount;
    private int followingCount;
    // Nouveau champ country
    private Country country;
}