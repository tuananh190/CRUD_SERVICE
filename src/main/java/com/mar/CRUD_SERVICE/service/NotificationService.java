package com.mar.CRUD_SERVICE.service;

import com.mar.CRUD_SERVICE.dto.response.NotificationResponse;
import com.mar.CRUD_SERVICE.model.Notification;
import com.mar.CRUD_SERVICE.model.NotificationType;
import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.repository.NotificationRepository;
import com.mar.CRUD_SERVICE.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public List<NotificationResponse> getAllNotifications(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + username));
        return notificationRepository.findByReceiverOrderByCreatedAtDesc(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<NotificationResponse> getUnreadNotifications(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + username));
        return notificationRepository.findByReceiverAndReadFalseOrderByCreatedAtDesc(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + username));
        return notificationRepository.countByReceiverAndReadFalse(user);
    }

    public Notification markAsRead(Long notificationId, String username) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông báo ID: " + notificationId));

        if (!notification.getReceiver().getUsername().equals(username)) {
            throw new IllegalStateException("Bạn không có quyền thao tác với thông báo này.");
        }

        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    public void deleteNotification(Long notificationId, String username) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông báo ID: " + notificationId));

        if (!notification.getReceiver().getUsername().equals(username)) {
            throw new IllegalStateException("Bạn không có quyền xóa thông báo này.");
        }

        notificationRepository.deleteById(notificationId);
    }

    public void createNotification(User receiver, User sender, NotificationType type, Long referenceId) {
        Notification notification = new Notification(receiver, sender, type, referenceId, java.time.LocalDateTime.now());
        notificationRepository.save(notification);
    }

    public NotificationResponse mapToResponse(Notification notification) {
        String senderUsername = notification.getSender() != null
                ? notification.getSender().getUsername()
                : "Ai đó";

        String message = buildMessage(notification.getType(), senderUsername);

        return new NotificationResponse(
                notification.getId(),
                notification.getType().name(),
                notification.getSender() != null ? notification.getSender().getId() : null,
                senderUsername,
                notification.getReferenceId(),
                notification.isRead(),
                notification.getCreatedAt(),
                message
        );
    }

    private String buildMessage(NotificationType type, String senderUsername) {
        return switch (type) {
            case LIKE          -> senderUsername + " đã thích bài viết của bạn.";
            case COMMENT       -> senderUsername + " đã bình luận bài viết của bạn.";
            case TAG           -> senderUsername + " đã nhắc đến bạn trong một bài viết hoặc bình luận.";
            case SHARE         -> senderUsername + " đã chia sẻ bài viết của bạn.";
            case FRIEND_REQUEST -> senderUsername + " đã gửi lời mời kết bạn đến bạn.";
        };
    }
}
