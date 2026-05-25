package com.mar.CRUD_SERVICE.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity lưu trữ quan hệ chặn (Block) giữa hai người dùng.
 *
 * Thiết kế "Plug-and-play":
 * - Bảng này hoàn toàn độc lập, không thay đổi schema của bảng users, posts, friendships.
 * - Toàn bộ logic filtering được thực thi ở Service layer, không ở DB layer.
 *
 * Business Rules (BR-Block):
 * - Quan hệ block là một chiều: A block B ≠ B block A
 * - Khi A block B: hệ thống phải tự động unfriend nếu đang là bạn bè (xử lý trong UserBlockService)
 * - Dữ liệu block được kiểm tra tại PostService và FriendshipService trước khi trả dữ liệu
 *
 * Performance Note:
 * - Cặp (blocker_id, blocked_id) được đánh UNIQUE INDEX → query kiểm tra block chạy O(log n)
 * - Index đơn trên blocked_id → hỗ trợ query "ai đã block tôi" nhanh hơn
 */
@Entity
@Table(
    name = "user_blocks",
    uniqueConstraints = {
        // Đảm bảo A chỉ có thể block B đúng một lần, tránh duplicate
        @UniqueConstraint(columnNames = {"blocker_id", "blocked_id"})
    },
    indexes = {
        // Index cho câu query: "Lấy tất cả người mà user này đã block" (dùng trong filter feed)
        @Index(name = "idx_block_blocker", columnList = "blocker_id"),
        // Index cho câu query: "Ai đã block user này?" (dùng khi kiểm tra send friend request)
        @Index(name = "idx_block_blocked", columnList = "blocked_id")
    }
)
public class UserBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Người thực hiện hành động chặn (chủ động).
     * Không dùng cascade để tránh xóa user kéo theo xóa block history.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_id", nullable = false)
    private User blocker;

    /**
     * Người bị chặn (bị động).
     * FetchType.LAZY để tránh load toàn bộ User object khi chỉ cần kiểm tra ID.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_id", nullable = false)
    private User blocked;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public UserBlock() {}

    public UserBlock(User blocker, User blocked) {
        this.blocker = blocker;
        this.blocked = blocked;
        this.createdAt = LocalDateTime.now();
    }

    // ==================== Getters & Setters ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getBlocker() { return blocker; }
    public void setBlocker(User blocker) { this.blocker = blocker; }

    public User getBlocked() { return blocked; }
    public void setBlocked(User blocked) { this.blocked = blocked; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
