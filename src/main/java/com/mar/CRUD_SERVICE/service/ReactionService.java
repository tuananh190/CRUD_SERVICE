package com.mar.CRUD_SERVICE.service;

import com.mar.CRUD_SERVICE.dto.response.ReactionResponse;
import com.mar.CRUD_SERVICE.model.Comment;
import com.mar.CRUD_SERVICE.model.Post;
import com.mar.CRUD_SERVICE.model.NotificationType;
import com.mar.CRUD_SERVICE.model.Reaction;
import com.mar.CRUD_SERVICE.model.ReactionType;
import com.mar.CRUD_SERVICE.model.Topic;
import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.model.UserInterest;
import com.mar.CRUD_SERVICE.repository.CommentRepository;
import com.mar.CRUD_SERVICE.repository.PostRepository;
import com.mar.CRUD_SERVICE.repository.ReactionRepository;
import com.mar.CRUD_SERVICE.repository.UserInterestRepository;
import com.mar.CRUD_SERVICE.repository.UserRepository;
import com.mar.CRUD_SERVICE.service.PostService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service xử lý nghiệp vụ Reaction (thả / đổi / huỷ cảm xúc).
 *
 * =====================================================================
 * THIẾT KẾ IDEMPOTENCY — GIẢI THÍCH CHO HỘI ĐỒNG
 * =====================================================================
 *
 * Vấn đề gốc (Technical Debt):
 *   Thiết kế ban đầu tồn tại hai nhóm API song song cho cùng mục đích:
 *   - Nhóm tổng quát: POST /reactions/post/{id}?type=LIKE
 *   - Nhóm Legacy: POST /reactions/post/{id}/like  (chỉ hỗ trợ LIKE)
 *   Cả hai đều gọi cùng Service method → không gây inconsistency ở DB layer,
 *   nhưng gây nhầm lẫn cho client và làm phức tạp tài liệu API.
 *
 * Giải pháp Idempotency (Toggle Pattern):
 *   Một operation được gọi là IDEMPOTENT nếu gọi nhiều lần cho ra
 *   cùng kết quả. Với Reaction, chúng ta áp dụng "Toggle Idempotency":
 *
 *   Lần gọi 1: user chưa có reaction → TẠO reaction (ADDED)
 *   Lần gọi 2: user đã có CÙNG loại → XÓA reaction (REMOVED) ← Toggle Off
 *   Lần gọi 3: user thả loại KHÁC → CẬP NHẬT loại reaction (CHANGED)
 *
 *   Điều này đảm bảo Data Consistency ở cả hai tầng:
 *   - DB Layer: @UniqueConstraint(user_id, post_id) ngăn duplicate ở tầng DB
 *   - Service Layer: findByUserAndPost() kiểm tra trước khi save
 *
 * Race Condition:
 *   Trong môi trường production với concurrent requests, @Transactional +
 *   UniqueConstraint là hàng rào cuối cùng. Nếu 2 request cùng lúc qua
 *   Service layer check, DB Constraint sẽ throw DataIntegrityViolationException.
 *   Trong scope ĐATN (Monolithic + Sequential), @Transactional là đủ.
 * =====================================================================
 */
@Service
public class ReactionService {

    private final ReactionRepository reactionRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final UserInterestRepository userInterestRepository;
    private final NotificationService notificationService;
    private final PostService postService;

    public ReactionService(ReactionRepository reactionRepository,
                           PostRepository postRepository,
                           CommentRepository commentRepository,
                           UserRepository userRepository,
                           UserInterestRepository userInterestRepository,
                           NotificationService notificationService,
                           @Lazy PostService postService) {
        this.reactionRepository = reactionRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.userInterestRepository = userInterestRepository;
        this.notificationService = notificationService;
        this.postService = postService;
    }

    // ================================================================
    // LUỒNG 1: React trên bài viết
    // Endpoint: POST /api/v1/reactions/post/{postId}?type=LIKE
    // ================================================================

    /**
     * Xử lý hành động thả reaction lên bài viết theo Toggle Pattern.
     *
     * Toggle Logic (3 trường hợp):
     *   CASE 1 - Chưa có reaction: Tạo mới → trả về action="ADDED"
     *   CASE 2 - Cùng loại đang có: Xóa đi   → trả về action="REMOVED" (Toggle Off)
     *   CASE 3 - Khác loại đang có: Đổi loại  → trả về action="CHANGED"
     *
     * @param postId   ID bài viết
     * @param username Username người thực hiện (từ JWT)
     * @param type     Loại reaction (LIKE, ANGRY...)
     * @return ReactionResponse với action, reactionType, totalReactions, breakdown
     */
    @Transactional
    public ReactionResponse reactToPost(Long postId, String username, ReactionType type) {
        // Bước 1: Validate và lấy entities từ DB
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + username));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết ID: " + postId));

        // Lỗi 2 (Privacy Bypass): Kiểm tra xem user này có quyền xem bài viết không trước khi thả tim
        if (!postService.canUserViewPost(user, post)) {
            throw new IllegalStateException("Bạn không có quyền tương tác với bài viết này.");
        }

        // Bước 2: Kiểm tra reaction hiện tại (1 user chỉ có tối đa 1 reaction / bài)
        // Đây là điểm then chốt của Idempotency: kiểm tra trước khi ghi
        Reaction existing = reactionRepository.findByUserAndPost(user, post).orElse(null);

        String action; // "ADDED" | "CHANGED" | "REMOVED"

        if (existing == null) {
            // CASE 1: Chưa react bao giờ → Tạo mới
            Reaction newReaction = new Reaction(user, post, null, type, LocalDateTime.now());
            reactionRepository.save(newReaction);
            action = "ADDED";

            // Gửi notification cho chủ bài viết (chỉ khi không phải tự react bài mình)
            if (post.getAuthor() != null && !post.getAuthor().getId().equals(user.getId())) {
                notificationService.createNotification(post.getAuthor(), user, NotificationType.LIKE, postId);
            }

            // Cập nhật interest score chỉ khi LIKE (Like = +1 điểm quan tâm)
            if (type == ReactionType.LIKE && post.getTopics() != null) {
                updateUserInterest(user, post.getTopics(), 1);
            }

        } else if (existing.getType() == type) {
            // CASE 2: Đã react cùng loại → Toggle Off (xóa reaction)
            // Đây là hành vi "Unlike" trong Facebook: nhấn Like lần 2 = bỏ Like
            reactionRepository.delete(existing);
            action = "REMOVED";
            // Không gửi notification khi bỏ reaction
            // Không cần xử lý interest khi bỏ (tránh phức tạp hoá logic điểm)

        } else {
            // CASE 3: Đã react khác loại → Cập nhật loại (LIKE → ANGRY hoặc ngược lại)
            // UPDATE thay vì DELETE+INSERT: giữ nguyên ID, chỉ đổi type và timestamp
            existing.setType(type);
            existing.setCreatedAt(LocalDateTime.now()); // reset timestamp để phản ánh thời điểm đổi
            reactionRepository.save(existing);
            action = "CHANGED";

            // Gửi notification cho chủ bài viết khi đổi loại reaction
            if (post.getAuthor() != null && !post.getAuthor().getId().equals(user.getId())) {
                notificationService.createNotification(post.getAuthor(), user, NotificationType.LIKE, postId);
            }
        }

        // Bước 3: Xây dựng response chi tiết (đọc lại từ DB sau khi save để đảm bảo chính xác)
        return buildPostReactionResponse(action, action.equals("REMOVED") ? null : type, postId);
    }

    // ================================================================
    // LUỒNG 2: React trên bình luận
    // Endpoint: POST /api/v1/reactions/comment/{commentId}?type=LIKE
    // ================================================================

    /**
     * Xử lý reaction trên comment — logic tương tự reactToPost (Toggle Pattern).
     *
     * @param commentId ID bình luận
     * @param username  Username người thực hiện
     * @param type      Loại reaction
     * @return ReactionResponse
     */
    @Transactional
    public ReactionResponse reactToComment(Long commentId, String username, ReactionType type) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + username));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy comment ID: " + commentId));

        Reaction existing = reactionRepository.findByUserAndComment(user, comment).orElse(null);
        String action;

        if (existing == null) {
            // CASE 1: Tạo mới
            Reaction newReaction = new Reaction(user, null, comment, type, LocalDateTime.now());
            reactionRepository.save(newReaction);
            action = "ADDED";

            if (comment.getAuthor() != null && !comment.getAuthor().getId().equals(user.getId())) {
                notificationService.createNotification(comment.getAuthor(), user, NotificationType.LIKE, commentId);
            }

            // interest từ topics của bài viết chứa comment này (nếu có)
            if (type == ReactionType.LIKE && comment.getPost() != null && comment.getPost().getTopics() != null) {
                updateUserInterest(user, comment.getPost().getTopics(), 1);
            }

        } else if (existing.getType() == type) {
            // CASE 2: Toggle Off
            reactionRepository.delete(existing);
            action = "REMOVED";

        } else {
            // CASE 3: Đổi loại
            existing.setType(type);
            existing.setCreatedAt(LocalDateTime.now());
            reactionRepository.save(existing);
            action = "CHANGED";

            if (comment.getAuthor() != null && !comment.getAuthor().getId().equals(user.getId())) {
                notificationService.createNotification(comment.getAuthor(), user, NotificationType.LIKE, commentId);
            }
        }

        return buildCommentReactionResponse(action, action.equals("REMOVED") ? null : type, commentId);
    }

    // ================================================================
    // PRIVATE HELPER METHODS
    // ================================================================

    /**
     * Xây dựng ReactionResponse cho bài viết.
     * Đọc lại số liệu từ DB sau khi transaction hoàn tất để đảm bảo chính xác.
     */
    private ReactionResponse buildPostReactionResponse(String action, ReactionType type, Long postId) {
        long total = reactionRepository.countByPostId(postId);
        Map<String, Long> breakdown = buildBreakdown(postId, true);
        return new ReactionResponse(
                action,
                type != null ? type.name() : null,
                total,
                breakdown
        );
    }

    /**
     * Xây dựng ReactionResponse cho bình luận.
     */
    private ReactionResponse buildCommentReactionResponse(String action, ReactionType type, Long commentId) {
        long total = reactionRepository.countByCommentId(commentId);
        Map<String, Long> breakdown = buildCommentBreakdown(commentId);
        return new ReactionResponse(
                action,
                type != null ? type.name() : null,
                total,
                breakdown
        );
    }

    /**
     * Đếm chi tiết từng loại reaction trên bài viết.
     * Chỉ đưa vào map những loại có count > 0 (tránh JSON dư thừa).
     */
    private Map<String, Long> buildBreakdown(Long postId, boolean isPost) {
        Map<String, Long> breakdown = new HashMap<>();
        for (ReactionType t : ReactionType.values()) {
            long count = reactionRepository.countByPostIdAndType(postId, t);
            if (count > 0) {
                breakdown.put(t.name(), count);
            }
        }
        return breakdown;
    }

    /**
     * Đếm chi tiết từng loại reaction trên comment.
     */
    private Map<String, Long> buildCommentBreakdown(Long commentId) {
        Map<String, Long> breakdown = new HashMap<>();
        for (ReactionType t : ReactionType.values()) {
            long count = reactionRepository.countByCommentIdAndType(commentId, t);
            if (count > 0) {
                breakdown.put(t.name(), count);
            }
        }
        return breakdown;
    }

    /**
     * Cập nhật điểm quan tâm của user theo từng topic (UserInterest system).
     */
    private void updateUserInterest(User user, List<Topic> topics, int weight) {
        for (Topic topic : topics) {
            UserInterest interest = userInterestRepository.findByUserAndTopic(user, topic)
                    .orElse(new UserInterest(user, topic, 0));
            interest.setScore(interest.getScore() + weight);
            userInterestRepository.save(interest);
        }
    }
}
