package theBugApp.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "info_user")
@Data @NoArgsConstructor @AllArgsConstructor
public class InfoUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true)
    private String password;

    @OneToOne(mappedBy = "infoUser")
    private User user;

    @Column(name = "oauth_provider")
    private String provider; // "google", "local", etc.

    @Column(name = "oauth_id")
    private String providerId; // ID unique fourni par Google
}
