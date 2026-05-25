package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.service.HiddenPostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * Controller xử lý Ẩn/Bỏ ẩn bài viết (Hide Post).
 *
 * Thiết kế URL theo Resource-Oriented (REST chuẩn):
 *   POST   /api/v1/posts/{id}/hide → Ẩn bài viết (tạo "hide" resource)
 *   DELETE /api/v1/posts/{id}/hide → Bỏ ẩn (xóa "hide" resource)
 *
 * URL đặt dưới /api/v1/posts/{id}/... để thể hiện quan hệ
 * "hide" là hành động con của một Post cụ thể.
 * Cách đặt URL này nhất quán với pattern /friendships/{id}/accept,
 * /reactions/post/{id}/like đã có trong dự án.
 *
 * Phân quyền: cả hai endpoint đều cần authenticated (anyRequest trong SecurityConfig).
 */
@RestController
@RequestMapping("/api/v1/posts")
public class HiddenPostController {

    private final HiddenPostService hiddenPostService;

    public HiddenPostController(HiddenPostService hiddenPostService) {
        this.hiddenPostService = hiddenPostService;
    }

    /**
     * POST /api/v1/posts/{postId}/hide
     * Ẩn một bài viết khỏi feed của người dùng đang đăng nhập.
     *
     * Bài viết bị ẩn:
     * - Không còn xuất hiện trong GET /api/v1/posts (feed)
     * - Không còn xuất hiện trong GET /api/v1/posts/trending
     * - Vẫn có thể truy cập trực tiếp qua GET /api/v1/posts/{id} nếu biết ID
     *   (hide chỉ loại khỏi feed, không phải xóa)
     *
     * Postman test:
     * - POST /api/v1/posts/1/hide + Authorization: Bearer <token>
     * - Kỳ vọng: 200 OK "Đã ẩn bài viết khỏi feed của bạn."
     * - Verify: GET /api/v1/posts → bài viết ID 1 không còn xuất hiện
     */
    @PostMapping("/{postId}/hide")
    public ResponseEntity<String> hidePost(@PathVariable Long postId, Principal principal) {
        try {
            String result = hiddenPostService.hidePost(principal.getName(), postId);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            // 409: Đã ẩn rồi, hoặc tự ẩn bài mình
            return ResponseEntity.status(409).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            // 404: Post không tồn tại
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/v1/posts/{postId}/hide
     * Hoàn tác hành động ẩn — bài viết xuất hiện lại trong feed.
     *
     * Postman test:
     * - DELETE /api/v1/posts/1/hide + Authorization: Bearer <token>
     * - Kỳ vọng: 200 OK "Đã hoàn tác ẩn bài viết..."
     * - Verify: GET /api/v1/posts → bài viết ID 1 xuất hiện lại
     */
    @DeleteMapping("/{postId}/hide")
    public ResponseEntity<String> unhidePost(@PathVariable Long postId, Principal principal) {
        try {
            String result = hiddenPostService.unhidePost(principal.getName(), postId);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
