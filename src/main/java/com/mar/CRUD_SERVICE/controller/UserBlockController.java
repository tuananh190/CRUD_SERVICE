package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.service.UserBlockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * Controller xử lý các request liên quan đến tính năng Chặn Người Dùng (Block).
 *
 * Tất cả endpoint đều yêu cầu xác thực (authenticated) — đã được cấu hình
 * trong SecurityConfig (.anyRequest().authenticated()).
 *
 * Convention: Theo đúng pattern của các Controller khác trong dự án:
 * - Constructor injection
 * - try-catch với IllegalStateException → 400 Bad Request
 * - try-catch với IllegalArgumentException → 404 Not Found
 * - Principal (từ JWT) để xác định user hiện tại
 */
@RestController
@RequestMapping("/api/v1/blocks")
public class UserBlockController {

    private final UserBlockService userBlockService;

    public UserBlockController(UserBlockService userBlockService) {
        this.userBlockService = userBlockService;
    }

    /**
     * POST /api/v1/blocks/{targetUserId}
     * Chặn một người dùng.
     *
     * Side-effects tự động:
     * - Hủy kết bạn nếu đang là bạn bè
     * - Hủy lời mời kết bạn đang PENDING
     *
     * Postman test:
     * - Header: Authorization: Bearer <token_của_A>
     * - URL: POST /api/v1/blocks/2
     * - Kết quả: 200 OK "Đã chặn người dùng @B thành công."
     */
    @PostMapping("/{targetUserId}")
    public ResponseEntity<String> blockUser(@PathVariable Long targetUserId,
                                            Principal principal) {
        try {
            String result = userBlockService.blockUser(principal.getName(), targetUserId);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            // Trả về 400 khi vi phạm business rule (đã block rồi, tự block mình, v.v.)
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            // Trả về 404 khi không tìm thấy user
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/v1/blocks/{targetUserId}
     * Gỡ chặn một người dùng.
     *
     * Lưu ý: Gỡ chặn KHÔNG tự động kết bạn lại.
     * Sau khi gỡ chặn, muốn kết bạn lại phải gửi lời mời qua /friendships/request.
     *
     * Postman test:
     * - Header: Authorization: Bearer <token_của_A>
     * - URL: DELETE /api/v1/blocks/2
     * - Kết quả: 200 OK "Đã bỏ chặn người dùng @B."
     */
    @DeleteMapping("/{targetUserId}")
    public ResponseEntity<String> unblockUser(@PathVariable Long targetUserId,
                                              Principal principal) {
        try {
            String result = userBlockService.unblockUser(principal.getName(), targetUserId);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/v1/blocks
     * Lấy danh sách người dùng mà tôi đang chặn.
     *
     * Chỉ trả về danh sách username (không expose blocked_id hay timestamp nội bộ).
     *
     * Postman test:
     * - Header: Authorization: Bearer <token_của_A>
     * - URL: GET /api/v1/blocks
     * - Kết quả: ["userB", "userC"]
     */
    @GetMapping
    public ResponseEntity<List<String>> getBlockedUsers(Principal principal) {
        List<String> blockedUsernames = userBlockService.getBlockedUsers(principal.getName());
        return ResponseEntity.ok(blockedUsernames);
    }
}
