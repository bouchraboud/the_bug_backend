package theBugApp.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "follow_questions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "question_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // Getters and Setters (or lombok @Data)
}
