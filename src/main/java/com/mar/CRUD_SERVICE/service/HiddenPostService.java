package com.mar.CRUD_SERVICE.service;

import com.mar.CRUD_SERVICE.model.HiddenPost;
import com.mar.CRUD_SERVICE.model.Post;
import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.repository.HiddenPostRepository;
import com.mar.CRUD_SERVICE.repository.PostRepository;
import com.mar.CRUD_SERVICE.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Service xử lý nghiệp vụ Ẩn bài viết (Hide Post).
 *
 * Thiết kế theo cùng pattern với UserBlockService:
 * - Nghiệp vụ đơn giản, không side-effect phức tạp
 * - Cung cấp utility method getHiddenPostIds() cho PostService sử dụng
 *   để filter feed mà KHÔNG cần inject HiddenPostService vào PostService
 *   nếu muốn tránh dependency thêm (xem ghi chú trong PostService)
 */
@Service
public class HiddenPostService {

    private final HiddenPostRepository hiddenPostRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public HiddenPostService(HiddenPostRepository hiddenPostRepository,
                             UserRepository userRepository,
                             PostRepository postRepository) {
        this.hiddenPostRepository = hiddenPostRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    // ================================================================
    // LUỒNG 1: Ẩn bài viết
    // Endpoint: POST /api/v1/posts/{postId}/hide
    // ================================================================

    /**
     * User ẩn một bài viết khỏi feed của mình.
     *
     * Logic:
     * - Không thể ẩn bài của chính mình (không có ý nghĩa thực tế)
     * - Idempotency: đã ẩn rồi thì throw exception (không tạo duplicate)
     * - UNIQUE constraint ở DB là lớp bảo vệ cuối cùng
     *
     * @param username Username của user đang đăng nhập (từ JWT)
     * @param postId   ID bài viết cần ẩn
     * @return Thông báo kết quả
     */
    @Transactional
    public String hidePost(String username, Long postId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + username));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết ID: " + postId));

        // Guard: không thể ẩn bài viết của chính mình
        if (post.getAuthor() != null && post.getAuthor().getId().equals(user.getId())) {
            throw new IllegalStateException("Bạn không thể ẩn bài viết của chính mình.");
        }

        // Idempotency guard: kiểm tra đã ẩn chưa (ở Service layer trước DB layer)
        if (hiddenPostRepository.existsByUserAndPost(user, post)) {
            throw new IllegalStateException("Bài viết này đã được ẩn khỏi feed của bạn rồi.");
        }

        hiddenPostRepository.save(new HiddenPost(user, post));
        return "Đã ẩn bài viết khỏi feed của bạn.";
    }

    // ================================================================
    // LUỒNG 2: Hoàn tác ẩn bài viết
    // Endpoint: DELETE /api/v1/posts/{postId}/hide
    // ================================================================

    /**
     * Hoàn tác hành động ẩn — bài viết xuất hiện lại trong feed.
     *
     * @param username Username của user
     * @param postId   ID bài viết cần bỏ ẩn
     * @return Thông báo kết quả
     */
    @Transactional
    public String unhidePost(String username, Long postId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + username));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết ID: " + postId));

        HiddenPost hiddenPost = hiddenPostRepository.findByUserAndPost(user, post)
                .orElseThrow(() -> new IllegalStateException("Bài viết này chưa được ẩn."));

        hiddenPostRepository.delete(hiddenPost);
        return "Đã hoàn tác ẩn bài viết. Bài viết sẽ xuất hiện lại trong feed của bạn.";
    }

    // ================================================================
    // UTILITY METHOD — Dùng bởi PostService để filter feed
    // ================================================================

    /**
     * Lấy tập hợp ID các bài viết mà user này đã ẩn.
     *
     * Được gọi trong PostService.getAllPosts() và getTrendingPosts() để filter feed.
     * Trả về Set<Long> (HashSet) để contains() O(1) khi duyệt qua danh sách bài viết.
     *
     * Nếu user = null (guest chưa đăng nhập) → trả về Set rỗng → không filter gì.
     *
     * @param user User đang xem feed (có thể null nếu chưa đăng nhập)
     * @return Set<Long> chứa các postId đã ẩn. Trả về empty Set nếu user = null.
     */
    public Set<Long> getHiddenPostIds(User user) {
        if (user == null) {
            return java.util.Collections.emptySet();
        }
        return hiddenPostRepository.findHiddenPostIdsByUser(user);
    }
}
