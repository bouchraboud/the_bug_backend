
package theBugApp.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import theBugApp.backend.enums.ReputationAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "reputation_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReputationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private ReputationAction action;

    @Column(name = "points", nullable = false)
    private int points;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // References to the content that triggered the reputation change
    @Column(name = "question_id")
    private Long questionId;

    @Column(name = "answer_id")
    private Long answerId;

    @Column(name = "vote_id")
    private Long voteId;

    @Column(name = "description")
    private String description;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public ReputationHistory(User user, ReputationAction action, int points) {
        this.user = user;
        this.action = action;
        this.points = points;
        this.description = action.getDescription();
    }
}