package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.dto.response.NotificationResponse;
import com.mar.CRUD_SERVICE.model.Notification;
import com.mar.CRUD_SERVICE.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // API 29: Lấy TẤT CẢ thông báo của user đang đăng nhập
    // Bảo mật: chỉ thấy thông báo của chính mình — không thể xem của người khác
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAllNotifications(Principal principal) {
        return ResponseEntity.ok(notificationService.getAllNotifications(principal.getName()));
    }

    // API 30: Lấy thông báo CHƯA ĐỌC của user đang đăng nhập
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(Principal principal) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(principal.getName()));
    }

    // API 30b: Đếm số thông báo chưa đọc — dùng cho badge 🔔
    // Ví dụ response: { "unread_count": 5 }
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Principal principal) {
        long count = notificationService.getUnreadCount(principal.getName());
        return ResponseEntity.ok(Map.of("unread_count", count));
    }

    // API 31: Đánh dấu thông báo đã đọc (theo ID)
    // Bảo mật: chỉ người nhận mới được đánh dấu
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, Principal principal) {
        try {
            Notification updated = notificationService.markAsRead(id, principal.getName());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // API 32: Xoá thông báo (theo ID)
    // Bảo mật: chỉ người nhận mới được xóa
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id, Principal principal) {
        try {
            notificationService.deleteNotification(id, principal.getName());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}
