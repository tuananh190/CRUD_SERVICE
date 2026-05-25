package com.mar.CRUD_SERVICE.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity lưu trữ các bài viết mà user đã chủ động ẩn khỏi feed của mình.
 *
 * =====================================================================
 * TẠI SAO THIẾT KẾ BẢNG RIÊNG — DATABASE NORMALIZATION (Chuẩn hóa CSDL)
 * =====================================================================
 *
 * Phương án bị loại bỏ: Thêm cột boolean `is_hidden` vào bảng `posts`
 *
 * Vấn đề của cột `is_hidden` trong bảng `posts`:
 *   → Vi phạm 1NF/2NF: "Ẩn bài viết" là thuộc tính của mối quan hệ
 *     (User, Post) chứ KHÔNG phải thuộc tính của Post.
 *     Bài viết A không phải "đã ẩn" — mà là "User X đã ẩn bài A khỏi feed của mình".
 *     User Y nhìn vào cùng bài A vẫn thấy bình thường.
 *
 *   → Không thể scale: 1 user ẩn = 1 record. Nếu dùng cột boolean,
 *     phải lưu riêng per-user per-post → không làm được với 1 cột.
 *
 * Phương án đúng: Bảng junction `hidden_posts` (User × Post)
 *   → Tuân thủ chuẩn hóa CSDL (3NF): mỗi bảng chỉ lưu thông tin
 *     thuộc về một entity duy nhất, mối quan hệ M:N được tách ra bảng riêng.
 *
 * Lợi ích về Performance:
 *   → Bảng `posts` KHÔNG bị tăng kích thước (không cột thêm) → scan posts nhanh hơn.
 *   → Khi filter feed, chỉ cần SELECT hidden_post_id WHERE user_id = ?
 *     → một SET nhỏ (~vài chục ID) để loại bỏ khỏi feed.
 *   → Index trên (user_id, post_id) đảm bảo lookup O(log n).
 * =====================================================================
 *
 * DB Impact: Tạo thêm bảng "hidden_posts" mới.
 * Bảng "posts" và "users" KHÔNG bị thay đổi.
 */
@Entity
@Table(
    name = "hidden_posts",
    uniqueConstraints = {
        // Mỗi user chỉ ẩn một bài viết đúng một lần (idempotency guard ở DB layer)
        @UniqueConstraint(columnNames = {"user_id", "post_id"})
    },
    indexes = {
        // Index then chốt: "Lấy tất cả bài user X đã ẩn" — dùng trong filter feed
        @Index(name = "idx_hidden_user", columnList = "user_id")
    }
)
public class HiddenPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User đã thực hiện hành động ẩn bài.
     * FetchType.LAZY: tránh load toàn bộ User khi chỉ cần user_id để filter.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Bài viết bị ẩn.
     * FetchType.LAZY: khi filter feed, chỉ cần post_id, không cần load Post object.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "hidden_at", nullable = false)
    private LocalDateTime hiddenAt;

    public HiddenPost() {}

    public HiddenPost(User user, Post post) {
        this.user = user;
        this.post = post;
        this.hiddenAt = LocalDateTime.now();
    }

    // ==================== Getters & Setters ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }

    public LocalDateTime getHiddenAt() { return hiddenAt; }
    public void setHiddenAt(LocalDateTime hiddenAt) { this.hiddenAt = hiddenAt; }
}
