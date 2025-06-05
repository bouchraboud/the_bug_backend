package theBugApp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import theBugApp.backend.entity.ReputationHistory;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReputationHistoryRepository extends JpaRepository<ReputationHistory, Long> {

    List<ReputationHistory> findByUserUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT SUM(rh.points) FROM ReputationHistory rh WHERE rh.user.userId = :userId AND rh.createdAt >= :startDate AND rh.createdAt < :endDate")
    Integer getTotalPointsForUserInPeriod(@Param("userId") Long userId,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT rh FROM ReputationHistory rh WHERE rh.user.userId = :userId AND rh.createdAt >= :startDate ORDER BY rh.createdAt DESC")
    List<ReputationHistory> findUserReputationSince(@Param("userId") Long userId,
                                                    @Param("startDate") LocalDateTime startDate);
}