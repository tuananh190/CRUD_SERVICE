package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.dto.response.ReactionResponse;
import com.mar.CRUD_SERVICE.model.ReactionType;
import com.mar.CRUD_SERVICE.service.ReactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * Controller xử lý Reaction (Cảm xúc) trên bài viết và bình luận.
 *
 * =====================================================================
 * QUYẾT ĐỊNH THIẾT KẾ — API CONSOLIDATION
 * =====================================================================
 *
 * Thiết kế ban đầu tồn tại hai nhóm API song song (Dual API problem):
 *
 * [NHÓM A - API Tổng quát] ← ĐÂY LÀ CHUẨN DUY NHẤT
 *   POST /api/v1/reactions/post/{postId}?type=LIKE    → hỗ trợ mọi loại reaction
 *   POST /api/v1/reactions/comment/{commentId}?type=LIKE
 *
 * [NHÓM B - Legacy API] ← ĐÃ ĐƯỢC DEPRECATED
 *   POST   /api/v1/reactions/post/{postId}/like      → chỉ hỗ trợ LIKE
 *   DELETE /api/v1/reactions/post/{postId}/like      → chỉ hỗ trợ Unlike
 *   (tương tự cho comment)
 *
 * Vấn đề của Dual API:
 *   - Vi phạm nguyên tắc DRY (Don't Repeat Yourself): hai API làm cùng việc
 *   - Legacy API không thể mở rộng (thêm ANGRY, LOVE phải tạo thêm endpoint)
 *   - Gây nhầm lẫn cho client: không biết nên dùng API nào
 *   - DELETE /like KHÔNG có nghĩa "xoá like" mà thực ra là "toggle like" → sai về ngữ nghĩa REST
 *
 * Giải pháp:
 *   - Giữ nguyên [NHÓM A] làm chuẩn duy nhất
 *   - Đánh dấu @Deprecated cho [NHÓM B], vẫn giữ để backward-compatible
 *     (tránh breaking change với client cũ nếu có)
 *   - Legacy endpoints giờ là thin wrapper → chuyển tiếp (delegate) sang NHÓM A
 * =====================================================================
 */
@RestController
@RequestMapping("/api/v1/reactions")
public class ReactionController {

    private final ReactionService reactionService;

    public ReactionController(ReactionService reactionService) {
        this.reactionService = reactionService;
    }

    // ================================================================
    // [NHÓM A] — API CHUẨN (sử dụng các endpoint này)
    // ================================================================

    /**
     * POST /api/v1/reactions/post/{postId}?type={reactionType}
     *
     * Thả / đổi / huỷ reaction trên bài viết theo Toggle Pattern.
     * Gọi lần 1 với type=LIKE → thả LIKE
     * Gọi lần 2 với type=LIKE → huỷ LIKE (Toggle Off)
     * Gọi lần 2 với type=ANGRY → đổi LIKE thành ANGRY
     *
     * @param postId    ID bài viết cần react
     * @param type      Loại reaction (LIKE | ANGRY)
     * @param principal JWT principal (lấy username tự động)
     * @return 200 OK với ReactionResponse chi tiết
     *         404 Not Found nếu bài viết không tồn tại
     *
     * Postman test:
     *   POST /api/v1/reactions/post/1?type=LIKE
     *   Authorization: Bearer <token>
     */
    @PostMapping("/post/{postId}")
    public ResponseEntity<ReactionResponse> reactToPost(@PathVariable Long postId,
                                                        @RequestParam("type") ReactionType type,
                                                        Principal principal) {
        try {
            ReactionResponse result = reactionService.reactToPost(postId, principal.getName(), type);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * POST /api/v1/reactions/comment/{commentId}?type={reactionType}
     *
     * Thả / đổi / huỷ reaction trên bình luận theo Toggle Pattern.
     *
     * @param commentId ID bình luận cần react
     * @param type      Loại reaction (LIKE | ANGRY)
     * @param principal JWT principal
     * @return 200 OK với ReactionResponse chi tiết
     */
    @PostMapping("/comment/{commentId}")
    public ResponseEntity<ReactionResponse> reactToComment(@PathVariable Long commentId,
                                                           @RequestParam("type") ReactionType type,
                                                           Principal principal) {
        try {
            ReactionResponse result = reactionService.reactToComment(commentId, principal.getName(), type);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ================================================================
    // [NHÓM B] — LEGACY API (Deprecated — giữ để backward-compatible)
    // Không xoá hoàn toàn để tránh breaking change với các client cũ.
    // Mỗi endpoint là thin wrapper, delegate sang API Nhóm A tương ứng.
    // ================================================================

    /**
     * @deprecated Sử dụng POST /api/v1/reactions/post/{postId}?type=LIKE
     * Legacy: Like bài viết (chỉ hỗ trợ LIKE, không hỗ trợ các loại reaction khác)
     */
    @Deprecated
    @PostMapping("/post/{postId}/like")
    public ResponseEntity<ReactionResponse> likePost(@PathVariable Long postId, Principal principal) {
        // Delegate sang API chuẩn với type=LIKE cố định
        return reactToPost(postId, ReactionType.LIKE, principal);
    }

    /**
     * @deprecated Sử dụng POST /api/v1/reactions/post/{postId}?type=LIKE (Toggle Off tự động)
     *
     * Legacy: Unlike bài viết.
     * Lưu ý ngữ nghĩa: DELETE method này thực chất là "toggle off" nếu đang LIKE,
     * NOT một hành động DELETE đúng nghĩa REST. Đây là một trong những lý do
     * endpoint này được deprecated — nó vi phạm ngữ nghĩa HTTP DELETE.
     */
    @Deprecated
    @DeleteMapping("/post/{postId}/like")
    public ResponseEntity<ReactionResponse> unlikePost(@PathVariable Long postId, Principal principal) {
        // Delegate: gọi toggle với LIKE → nếu đang LIKE thì sẽ bị xoá (REMOVED)
        return reactToPost(postId, ReactionType.LIKE, principal);
    }

    /**
     * @deprecated Sử dụng POST /api/v1/reactions/comment/{commentId}?type=LIKE
     */
    @Deprecated
    @PostMapping("/comment/{commentId}/like")
    public ResponseEntity<ReactionResponse> likeComment(@PathVariable Long commentId, Principal principal) {
        return reactToComment(commentId, ReactionType.LIKE, principal);
    }

    /**
     * @deprecated Sử dụng POST /api/v1/reactions/comment/{commentId}?type=LIKE (Toggle Off)
     */
    @Deprecated
    @DeleteMapping("/comment/{commentId}/like")
    public ResponseEntity<ReactionResponse> unlikeComment(@PathVariable Long commentId, Principal principal) {
        return reactToComment(commentId, ReactionType.LIKE, principal);
    }
}
