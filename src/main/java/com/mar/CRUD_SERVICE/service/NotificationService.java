package com.mar.CRUD_SERVICE.service;

import com.mar.CRUD_SERVICE.model.Notification;
import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.repository.NotificationRepository;
import com.mar.CRUD_SERVICE.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public List<Notification> getAllNotifications(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + username));
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
    }

    public List<Notification> getUnreadNotifications(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + username));
        return notificationRepository.findByRecipientAndReadFalseOrderByCreatedAtDesc(user);
    }

    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông báo ID: " + notificationId));
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    public void deleteNotification(Long notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new IllegalArgumentException("Không tìm thấy thông báo ID: " + notificationId);
        }
        notificationRepository.deleteById(notificationId);
    }

    // Helper: tạo thông báo từ các service khác
    public void createNotification(User recipient, String message) {
        Notification notification = new Notification(recipient, message, java.time.LocalDateTime.now());
        notificationRepository.save(notification);
    }
}
