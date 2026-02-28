package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // API 35: Xem trang cá nhân của một user bất kỳ
    @GetMapping("/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    // Không trả về password
                    user.setPassword("[PROTECTED]");
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // API 36: Cập nhật profile của bản thân (bio, avatarUrl)
    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> updates, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + principal.getName()));

        if (updates.containsKey("bio")) {
            user.setBio(updates.get("bio"));
        }
        if (updates.containsKey("avatarUrl")) {
            user.setAvatarUrl(updates.get("avatarUrl"));
        }
        if (updates.containsKey("firstName")) {
            user.setFirstName(updates.get("firstName"));
        }
        if (updates.containsKey("lastName")) {
            user.setLastName(updates.get("lastName"));
        }

        userRepository.save(user);
        user.setPassword("[PROTECTED]");
        return ResponseEntity.ok(user);
    }
}
