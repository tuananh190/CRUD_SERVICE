package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.dto.response.NotificationResponse;
import com.mar.CRUD_SERVICE.model.Notification;
import com.mar.CRUD_SERVICE.service.NotificationService;
import com.mar.CRUD_SERVICE.dto.response.ApiResponse;
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

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAllNotifications(Principal principal) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy danh sách thông báo thành công", notificationService.getAllNotifications(principal.getName())));
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications(Principal principal) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy danh sách thông báo chưa đọc thành công", notificationService.getUnreadNotifications(principal.getName())));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(Principal principal) {
        long count = notificationService.getUnreadCount(principal.getName());
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy số lượng thông báo thành công", Map.of("unread_count", count)));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Notification>> markAsRead(@PathVariable Long id, Principal principal) {
        Notification updated = notificationService.markAsRead(id, principal.getName());
        return ResponseEntity.ok(new ApiResponse<>(200, "Đánh dấu đã đọc thành công", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteNotification(@PathVariable Long id, Principal principal) {
        notificationService.deleteNotification(id, principal.getName());
        return ResponseEntity.ok(new ApiResponse<>(200, "Xóa thông báo thành công", null));
    }
}
