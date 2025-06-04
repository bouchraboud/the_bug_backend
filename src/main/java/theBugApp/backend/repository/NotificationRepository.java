package theBugApp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import theBugApp.backend.entity.FollowTag;
import theBugApp.backend.entity.Notification;
import theBugApp.backend.entity.Question;

import org.springframework.data.domain.Pageable;
import theBugApp.backend.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserUserIdAndIsRead(Long userId, Boolean isRead);
    List<Notification> findByUserUserIdOrderByTimestampDesc(Long userId);
    List<Notification> findByUser_InfoUser_Email(String email);

}