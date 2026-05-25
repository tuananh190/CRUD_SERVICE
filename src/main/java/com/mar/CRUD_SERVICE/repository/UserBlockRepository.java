package com.mar.CRUD_SERVICE.repository;

import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.model.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository xử lý truy vấn bảng user_blocks.
 *
 * Tất cả query ở đây đều được hỗ trợ bởi Index → tốc độ O(log n).
 *
 * JPQL được ưu tiên thay vì native SQL để dễ maintain và portable sang H2 khi test.
 */
@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {

    /**
     * Kiểm tra A có đang block B không.
     * Đây là câu query QUAN TRỌNG NHẤT — được gọi mỗi khi cần filter nội dung.
     *
     * Dùng boolean trực tiếp thay vì Optional để tiện dùng trong điều kiện if.
     * Performance: sử dụng INDEX trên (blocker_id, blocked_id) → cực nhanh.
     */
    @Query("SELECT COUNT(b) > 0 FROM UserBlock b WHERE b.blocker = :blocker AND b.blocked = :blocked")
    boolean existsByBlockerAndBlocked(@Param("blocker") User blocker, @Param("blocked") User blocked);

    /**
     * Kiểm tra quan hệ block theo cả hai chiều: A block B HOẶC B block A.
     * Dùng để kiểm tra toàn diện trước khi cho phép tương tác (comment, react, friend request).
     *
     * Ví dụ: A block B → B cũng không được gửi friend request cho A.
     */
    @Query("SELECT COUNT(b) > 0 FROM UserBlock b WHERE (b.blocker = :u1 AND b.blocked = :u2) OR (b.blocker = :u2 AND b.blocked = :u1)")
    boolean existsBlockBetween(@Param("u1") User u1, @Param("u2") User u2);

    /**
     * Lấy bản ghi block cụ thể để thực hiện unblock (delete).
     * Trả về Optional để xử lý trường hợp không tìm thấy một cách graceful.
     */
    @Query("SELECT b FROM UserBlock b WHERE b.blocker = :blocker AND b.blocked = :blocked")
    Optional<UserBlock> findByBlockerAndBlocked(@Param("blocker") User blocker, @Param("blocked") User blocked);

    /**
     * Lấy danh sách tất cả user mà người này đã block.
     * Dùng cho API GET /api/v1/blocks — trả về danh sách người bị chặn.
     *
     * Performance note: Nếu danh sách block rất lớn (> 1000), cần thêm Pageable.
     * Trong phạm vi ĐATN với dữ liệu test, dùng List là hợp lý.
     */
    @Query("SELECT b FROM UserBlock b WHERE b.blocker = :blocker ORDER BY b.createdAt DESC")
    List<UserBlock> findAllByBlocker(@Param("blocker") User blocker);

    /**
     * Lấy danh sách ID của các user mà người này đã block.
     * Trả về List<Long> (chỉ ID) thay vì toàn bộ UserBlock object để tối ưu bộ nhớ.
     * Được dùng trong PostService để filter feed nhanh hơn.
     */
    @Query("SELECT b.blocked.id FROM UserBlock b WHERE b.blocker = :blocker")
    List<Long> findBlockedUserIdsByBlocker(@Param("blocker") User blocker);
}
