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
    public ResponseEntity<?> getProfile(@PathVariable Long userId) {
        return userService.getUserById(userId)
                .map(user -> {

                    user.setPassword("[PROTECTED]");
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> updates, Principal principal) {
        try {
            User user = userService.updateProfile(principal.getName(), updates);

            user.setPassword("[PROTECTED]");
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/privacy")
    public ResponseEntity<?> updatePrivacy(@RequestBody Map<String, Boolean> body, Principal principal) {
        try {
            Boolean isPrivate = body.get("isPrivate");
            if (isPrivate == null) {
                return ResponseEntity.badRequest().body("Thiếu trường 'isPrivate' trong request body.");
            }
            User user = userService.updatePrivacy(principal.getName(), isPrivate);
            user.setPassword("[PROTECTED]");
            String message = isPrivate
                    ? "Trang cá nhân đã được khóa. Chỉ bạn bè mới xem được bài viết của bạn."
                    : "Trang cá nhân đã được mở công khai.";
            return ResponseEntity.ok(new ApiResponse<>(200, message, Map.of("isPrivate", user.isPrivate())));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
