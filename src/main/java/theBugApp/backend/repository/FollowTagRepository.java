package theBugApp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import theBugApp.backend.entity.FollowTag;
import theBugApp.backend.entity.Question;

import org.springframework.data.domain.Pageable;
import theBugApp.backend.entity.Tag;
import theBugApp.backend.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowTagRepository extends JpaRepository<FollowTag, Long> {
    // Changed from findByUserId to findByUserUserId to match the actual field name
    List<FollowTag> findByUserUserId(Long userId);

    // Added custom query for finding followers by tag ID
    @Query("SELECT ft.user FROM FollowTag ft WHERE ft.tag.id = :tagId")
    List<User> findFollowersByTagId(@Param("tagId") Long tagId);
    Optional<FollowTag> findByUserAndTag(User user, Tag tag);


}