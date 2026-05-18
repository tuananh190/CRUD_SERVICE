package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    // ✅ Đúng kiến trúc: Controller → Service → Repository
    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    // API 35: Xem trang cá nhân của một user bất kỳ (theo userId)
    @GetMapping("/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable Long userId) {
        return userService.getUserById(userId)
                .map(user -> {
                    // Không trả về password ra ngoài
                    user.setPassword("[PROTECTED]");
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // API 36: Cập nhật profile của bản thân (bio, avatarUrl, firstName, lastName)
    // Bảo mật: chỉ cập nhật được profile của chính mình — username lấy từ JWT token
    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> updates, Principal principal) {
        try {
            User user = userService.updateProfile(principal.getName(), updates);
            // Không trả về password ra ngoài
            user.setPassword("[PROTECTED]");
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // API 37: Bật/tắt khóa trang cá nhân
    // isPrivate = true  → Chỉ bạn bè mới xem được bài viết của bạn
    // isPrivate = false → Trang công khai (mặc định)
    // Body: { "isPrivate": true } hoặc { "isPrivate": false }
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
            return ResponseEntity.ok(Map.of("message", message, "isPrivate", user.isPrivate()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
