package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.model.Notification;
import com.mar.CRUD_SERVICE.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // API 29: Lấy tất cả thông báo của user hiện tại
    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications(Principal principal) {
        return ResponseEntity.ok(notificationService.getAllNotifications(principal.getName()));
    }

    // API 30: Lấy thông báo chưa đọc
    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(Principal principal) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(principal.getName()));
    }

    // API 31: Đánh dấu thông báo đã đọc
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        try {
            Notification updated = notificationService.markAsRead(id);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // API 32: Xoá thông báo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        try {
            notificationService.deleteNotification(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
