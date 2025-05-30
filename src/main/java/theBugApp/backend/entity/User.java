package theBugApp.backend.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "app_user_id", referencedColumnName = "userId")
    @EqualsAndHashCode.Exclude  // <-- Exclure cette relation
    private InfoUser infoUser;

    private String photoUrl;

    private int reputation = 0;

    @Version
    private Long version;

    private boolean isConfirmed;
    // In User.java
    @OneToMany(mappedBy = "user")
    private Set<Question> questions = new HashSet<>();

    // In User.java
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Answer> answers = new HashSet<>();
}
