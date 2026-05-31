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
    public ResponseEntity<ApiResponse<String>> blockUser(@PathVariable Long targetUserId,
                                            Principal principal) {
        try {
            String result = userBlockService.blockUser(principal.getName(), targetUserId);
            return ResponseEntity.ok(new ApiResponse<>(200, result, null));
        } catch (IllegalStateException e) {

            return ResponseEntity.badRequest().body(new ApiResponse<>(400, e.getMessage(), null));
        } catch (IllegalArgumentException e) {

            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{targetUserId}")
    public ResponseEntity<ApiResponse<String>> unblockUser(@PathVariable Long targetUserId,
                                              Principal principal) {
        try {
            String result = userBlockService.unblockUser(principal.getName(), targetUserId);
            return ResponseEntity.ok(new ApiResponse<>(200, result, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(new ApiResponse<>(404, e.getMessage(), null));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<List<String>> getBlockedUsers(Principal principal) {
        List<String> blockedUsernames = userBlockService.getBlockedUsers(principal.getName());
        return ResponseEntity.ok(blockedUsernames);
    }
}
