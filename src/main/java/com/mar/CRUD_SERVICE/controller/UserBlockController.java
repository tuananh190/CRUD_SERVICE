package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.service.UserBlockService;
import com.mar.CRUD_SERVICE.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/blocks")
public class UserBlockController {

    private final UserBlockService userBlockService;

    public UserBlockController(UserBlockService userBlockService) {
        this.userBlockService = userBlockService;
    }

    @PostMapping("/{targetUserId}")
    public ResponseEntity<ApiResponse<String>> blockUser(@PathVariable Long targetUserId, Principal principal) {
        String result = userBlockService.blockUser(principal.getName(), targetUserId);
        return ResponseEntity.ok(new ApiResponse<>(200, result, null));
    }

    @DeleteMapping("/{targetUserId}")
    public ResponseEntity<ApiResponse<String>> unblockUser(@PathVariable Long targetUserId, Principal principal) {
        String result = userBlockService.unblockUser(principal.getName(), targetUserId);
        return ResponseEntity.ok(new ApiResponse<>(200, result, null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<String>>> getBlockedUsers(Principal principal) {
        List<String> blockedUsernames = userBlockService.getBlockedUsers(principal.getName());
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy danh sách người dùng bị chặn thành công", blockedUsernames));
    }
}
