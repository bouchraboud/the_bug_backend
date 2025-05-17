package theBugApp.backend.dto;
import lombok.Data;

@Data
public class InfoUserDto {
    private Long userId;
    private String username;
    private String email;
    // Pas de champ 'user' ici
}