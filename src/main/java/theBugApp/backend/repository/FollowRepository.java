package theBugApp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import theBugApp.backend.entity.Follow;
import theBugApp.backend.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    // Vérifier si user1 suit user2
    boolean existsByFollowerAndFollowing(User follower, User following);

    // Trouver la relation de suivi
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);

    // Obtenir tous les followers d'un utilisateur
    List<Follow> findByFollowing(User following);

    // Obtenir tous ceux qu'un utilisateur suit
    List<Follow> findByFollower(User follower);

    // Compter les followers
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.following.userId = :userId")
    long countFollowersByUserId(@Param("userId") Long userId);

    // Compter les following
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.follower.userId = :userId")
    long countFollowingByUserId(@Param("userId") Long userId);

    // Supprimer une relation de suivi
    void deleteByFollowerAndFollowing(User follower, User following);
}