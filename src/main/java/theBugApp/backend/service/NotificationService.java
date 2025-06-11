package theBugApp.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import theBugApp.backend.entity.*;
import theBugApp.backend.enums.NotificationType;
import theBugApp.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Set;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final FollowQuestionRepository followQuestionRepository;
    private final FollowTagRepository followTagRepository;
    private final FollowAnswerRepository followAnswerRepository;

    @Autowired
    public NotificationService(
            UserRepository userRepository,
            NotificationRepository notificationRepository,
            FollowQuestionRepository followQuestionRepository,
            FollowTagRepository followTagRepository,
            FollowAnswerRepository followAnswerRepository) {
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.followQuestionRepository = followQuestionRepository;
        this.followTagRepository = followTagRepository;
        this.followAnswerRepository = followAnswerRepository;
    }

    public void sendNotification(Long userId, NotificationType type, Long referenceId, String message) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

            Notification notification = new Notification();
            notification.setUser(user);
            notification.setType(type);
            notification.setReferenceId(referenceId);
            notification.setMessage(message);
            notification.setIsRead(false);

            notificationRepository.save(notification);
            logger.info("Notification sent successfully to user: {}", userId);
        } catch (Exception e) {
            logger.error("Failed to send notification to user: {}", userId, e);
        }
    }

    // Existing method - notify followers when new answer is added to followed question
    public void notifyNewAnswer(Long questionId, Answer answer) {
        try {
            List<User> followers = followQuestionRepository.findFollowersByQuestionId(questionId);

            List<Notification> notifications = followers.stream()
                    .filter(user -> !user.getUserId().equals(answer.getUser().getUserId())) // Don't notify the answer author
                    .map(user -> {
                        Notification notification = new Notification();
                        notification.setUser(user);
                        notification.setType(NotificationType.ANSWER);
                        notification.setReferenceId(answer.getId());
                        notification.setMessage("A new answer was added to a question you follow!");
                        notification.setIsRead(false);
                        return notification;
                    })
                    .toList();

            notificationRepository.saveAll(notifications);
            logger.info("Notifications sent for question ID: {} to {} followers", questionId, followers.size());
        } catch (Exception e) {
            logger.error("Failed to send notifications for new answer on question: {}", questionId, e);
        }
    }

    // New method - notify followers when new question is created with followed tags
    public void notifyNewQuestionWithTags(Question question) {
        try {
            Set<Tag> tags = question.getTags();

            for (Tag tag : tags) {
                List<User> tagFollowers = followTagRepository.findFollowersByTagId(tag.getId());

                List<Notification> notifications = tagFollowers.stream()
                        .filter(user -> !user.getUserId().equals(question.getUser().getUserId())) // Don't notify the question author
                        .map(user -> {
                            Notification notification = new Notification();
                            notification.setUser(user);
                            notification.setType(NotificationType.QUESTION);
                            notification.setReferenceId(question.getId());
                            notification.setMessage("A new question was posted with tag '" + tag.getName() + "' that you follow!");
                            notification.setIsRead(false);
                            return notification;
                        })
                        .toList();

                notificationRepository.saveAll(notifications);
                logger.info("Notifications sent for new question with tag '{}' to {} followers", tag.getName(), tagFollowers.size());
            }
        } catch (Exception e) {
            logger.error("Failed to send notifications for new question with tags: {}", question.getId(), e);
        }
    }

    // New method - notify followers when answer is updated/edited
    public void notifyAnswerUpdate(Answer answer) {
        try {
            List<User> followers = followAnswerRepository.findFollowersByAnswerId(answer.getId());

            List<Notification> notifications = followers.stream()
                    .filter(user -> !user.getUserId().equals(answer.getUser().getUserId())) // Don't notify the answer author
                    .map(user -> {
                        Notification notification = new Notification();
                        notification.setUser(user);
                        notification.setType(NotificationType.ANSWER);
                        notification.setReferenceId(answer.getId());
                        notification.setMessage("An answer you follow has been updated!");
                        notification.setIsRead(false);
                        return notification;
                    })
                    .toList();

            notificationRepository.saveAll(notifications);
            logger.info("Notifications sent for answer update ID: {} to {} followers", answer.getId(), followers.size());
        } catch (Exception e) {
            logger.error("Failed to send notifications for answer update: {}", answer.getId(), e);
        }
    }

    // New method - notify followers when question is updated/edited
    public void notifyQuestionUpdate(Question question) {
        try {
            List<User> followers = followQuestionRepository.findFollowersByQuestionId(question.getId());

            List<Notification> notifications = followers.stream()
                    .filter(user -> !user.getUserId().equals(question.getUser().getUserId())) // Don't notify the question author
                    .map(user -> {
                        Notification notification = new Notification();
                        notification.setUser(user);
                        notification.setType(NotificationType.QUESTION);
                        notification.setReferenceId(question.getId());
                        notification.setMessage("A question you follow has been updated!");
                        notification.setIsRead(false);
                        return notification;
                    })
                    .toList();

            notificationRepository.saveAll(notifications);
            logger.info("Notifications sent for question update ID: {} to {} followers", question.getId(), followers.size());
        } catch (Exception e) {
            logger.error("Failed to send notifications for question update: {}", question.getId(), e);
        }
    }

    // New method - notify when answer is accepted (for question followers)
    public void notifyAnswerAccepted(Answer answer) {
        try {
            List<User> questionFollowers = followQuestionRepository.findFollowersByQuestionId(answer.getQuestion().getId());
            List<User> answerFollowers = followAnswerRepository.findFollowersByAnswerId(answer.getId());

            // Notify question followers
            List<Notification> questionNotifications = questionFollowers.stream()
                    .filter(user -> !user.getUserId().equals(answer.getUser().getUserId()) &&
                            !user.getUserId().equals(answer.getQuestion().getUser().getUserId()))
                    .map(user -> {
                        Notification notification = new Notification();
                        notification.setUser(user);
                        notification.setType(NotificationType.ANSWER);
                        notification.setReferenceId(answer.getId());
                        notification.setMessage("An answer has been accepted for a question you follow!");
                        notification.setIsRead(false);
                        return notification;
                    })
                    .toList();

            // Notify answer followers
            List<Notification> answerNotifications = answerFollowers.stream()
                    .filter(user -> !user.getUserId().equals(answer.getUser().getUserId()))
                    .map(user -> {
                        Notification notification = new Notification();
                        notification.setUser(user);
                        notification.setType(NotificationType.ANSWER);
                        notification.setReferenceId(answer.getId());
                        notification.setMessage("An answer you follow has been accepted!");
                        notification.setIsRead(false);
                        return notification;
                    })
                    .toList();

            notificationRepository.saveAll(questionNotifications);
            notificationRepository.saveAll(answerNotifications);

            logger.info("Acceptance notifications sent for answer ID: {} to {} question followers and {} answer followers",
                    answer.getId(), questionNotifications.size(), answerNotifications.size());
        } catch (Exception e) {
            logger.error("Failed to send notifications for answer acceptance: {}", answer.getId(), e);
        }
    }

    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserUserIdOrderByTimestampDesc(userId);
    }

    public List<Notification> getUnreadNotificationsForUser(Long userId) {
        return notificationRepository.findByUserUserIdAndIsRead(userId, false);
    }

    public void markNotificationAsRead(Long notificationId) {
        try {
            notificationRepository.findById(notificationId).ifPresent(notification -> {
                notification.setIsRead(true);
                notificationRepository.save(notification);
            });
        } catch (Exception e) {
            logger.error("Failed to mark notification as read: {}", notificationId, e);
        }
    }

    public void markAllNotificationsAsRead(Long userId) {
        try {
            List<Notification> unreadNotifications = getUnreadNotificationsForUser(userId);
            unreadNotifications.forEach(notification -> notification.setIsRead(true));
            notificationRepository.saveAll(unreadNotifications);
        } catch (Exception e) {
            logger.error("Failed to mark all notifications as read for user: {}", userId, e);
        }
    }

    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));

        if (!notification.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("User not authorized to delete this notification");
        }
        notificationRepository.delete(notification);
    }

}