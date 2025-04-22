package theBugApp.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserConfirmationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;
    private Date createdDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @Version
    private Long version;
    public UserConfirmationToken(String token, User user) {
        this.token = token;
        this.user = user;
        this.createdDate = new Date();
    }


}