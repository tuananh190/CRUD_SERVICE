package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.service.UserService;
import com.mar.CRUD_SERVICE.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<User>> getProfile(@PathVariable Long userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new com.mar.CRUD_SERVICE.exception.ResourceNotFoundException("Không tìm thấy người dùng"));
        user.setPassword("[PROTECTED]");
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy thông tin thành công", user));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<User>> updateProfile(@RequestBody Map<String, String> updates, Principal principal) {
        User user = userService.updateProfile(principal.getName(), updates);
        user.setPassword("[PROTECTED]");
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật hồ sơ thành công", user));
    }

    @PutMapping("/privacy")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> updatePrivacy(@RequestBody Map<String, Boolean> body, Principal principal) {
        Boolean isPrivate = body.get("isPrivate");
        if (isPrivate == null) {
            throw new com.mar.CRUD_SERVICE.exception.BadRequestException("Thiếu trường 'isPrivate' trong request body.");
        }
        User user = userService.updatePrivacy(principal.getName(), isPrivate);
        user.setPassword("[PROTECTED]");
        String message = isPrivate
                ? "Trang cá nhân đã được khóa. Chỉ bạn bè mới xem được bài viết của bạn."
                : "Trang cá nhân đã được mở công khai.";
        return ResponseEntity.ok(new ApiResponse<>(200, message, Map.of("isPrivate", user.isPrivate())));
    }
}
