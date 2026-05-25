package com.mar.CRUD_SERVICE.repository;

import com.mar.CRUD_SERVICE.model.HiddenPost;
import com.mar.CRUD_SERVICE.model.Post;
import com.mar.CRUD_SERVICE.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository truy vấn bảng "hidden_posts".
 *
 * Chiến lược query:
 * - Trả về Set<Long> (chỉ ID) thay vì List<HiddenPost> trong hot path
 *   → tiết kiệm bộ nhớ, tránh load Post/User object không cần thiết.
 * - contains() trên HashSet là O(1) → filter N bài viết chỉ cần N lần lookup.
 */
@Repository
public interface HiddenPostRepository extends JpaRepository<HiddenPost, Long> {

    /**
     * Kiểm tra user đã ẩn bài viết này chưa.
     * Dùng để guard trước khi lưu (tránh duplicate, dù UNIQUE constraint đã backup).
     */
    boolean existsByUserAndPost(User user, Post post);

    /**
     * Tìm bản ghi ẩn cụ thể để thực hiện unhide (delete).
     */
    Optional<HiddenPost> findByUserAndPost(User user, Post post);

    /**
     * Lấy SET ID của các bài viết mà user này đã ẩn.
     *
     * Đây là query then chốt được gọi trong getAllPosts() và getTrendingPosts().
     * Trả về Set<Long> thay vì List<HiddenPost> để:
     *   1. Giảm data transfer từ DB (chỉ lấy ID, không load Post objects)
     *   2. HashSet.contains(id) là O(1) → filter feed N bài = O(N) tổng cộng
     *
     * Sử dụng Index idx_hidden_user(user_id) → query O(log n) trên bảng hidden_posts.
     */
    @Query("SELECT h.post.id FROM HiddenPost h WHERE h.user = :user")
    Set<Long> findHiddenPostIdsByUser(@Param("user") User user);
}
