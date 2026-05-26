package com.mar.CRUD_SERVICE.service;

import com.mar.CRUD_SERVICE.model.Friendship;
import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.model.UserBlock;
import com.mar.CRUD_SERVICE.repository.FriendshipRepository;
import com.mar.CRUD_SERVICE.repository.UserBlockRepository;
import com.mar.CRUD_SERVICE.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service xử lý toàn bộ nghiệp vụ liên quan đến Block User.
 *
 * Kiến trúc: Tất cả side-effects (auto-unfriend, v.v.) được xử lý
 * trong cùng một @Transactional → đảm bảo tính toàn vẹn dữ liệu (atomicity).
 * Nếu bất kỳ bước nào thất bại, toàn bộ giao dịch sẽ rollback.
 */
@Service
public class UserBlockService {

    private final UserBlockRepository userBlockRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    // Constructor injection — đây là cách Spring Boot khuyến nghị thay vì @Autowired field injection
    public UserBlockService(UserBlockRepository userBlockRepository,
                            UserRepository userRepository,
                            FriendshipRepository friendshipRepository) {
        this.userBlockRepository = userBlockRepository;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    // ============================================================
    // LUỒNG 1: Block người dùng
    // Endpoint: POST /api/v1/blocks/{targetUserId}
    // ============================================================

    /**
     * Thực hiện chặn người dùng với đầy đủ side-effects.
     *
     * Quy trình xử lý (theo thứ tự):
     * 1. Validate input (self-block, already-blocked)
     * 2. Tạo bản ghi UserBlock
     * 3. [Side-effect] Tự động hủy kết bạn nếu đang là bạn bè (BR-Block-01)
     * 4. [Side-effect] Xóa bất kỳ lời mời kết bạn đang PENDING giữa hai người (BR-Block-02)
     *
     * @param blockerUsername Username của người thực hiện block (lấy từ JWT)
     * @param targetUserId    ID của người bị block
     * @return Thông báo kết quả
     */
    @Transactional // Đảm bảo toàn bộ 4 bước trên chạy trong một transaction duy nhất
    public String blockUser(String blockerUsername, Long targetUserId) {
        // Bước 1a: Lấy thông tin blocker từ DB dựa vào username trong JWT
        User blocker = userRepository.findByUsername(blockerUsername)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + blockerUsername));

        // Bước 1b: Lấy thông tin người bị block
        User blocked = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user ID: " + targetUserId));

        // Bước 1c: Validate — không thể tự block chính mình
        if (blocker.getId().equals(blocked.getId())) {
            throw new IllegalStateException("Bạn không thể tự chặn chính mình.");
        }

        // Bước 1d: Validate — không block người đã block rồi (idempotency guard)
        if (userBlockRepository.existsByBlockerAndBlocked(blocker, blocked)) {
            throw new IllegalStateException("Bạn đã chặn người dùng này rồi.");
        }

        // Bước 2: Tạo và lưu bản ghi block vào DB
        UserBlock newBlock = new UserBlock(blocker, blocked);
        userBlockRepository.save(newBlock);

        // Bước 3 & 4 [Side-effect - BR-Block-01 & BR-Block-02]: Xử lý quan hệ bạn bè/lời mời còn tồn tại.
        // findByUsers trả về List<Friendship> (kiểm tra cả hai chiều: blocker→blocked VÀ blocked→blocker).
        // Chỉ query MỘT LẦN để tránh race condition giữa delete (bước 3) và query lại (bước 4).
        List<Friendship> existingRelations = friendshipRepository.findByUsers(blocker, blocked);
        if (!existingRelations.isEmpty()) {
            Friendship existingFriendship = existingRelations.get(0);
            String status = existingFriendship.getStatus();

            if ("ACCEPTED".equals(status)) {
                // BR-Block-01: Xóa hẳn quan hệ bạn bè, không giữ lại history khi block
                friendshipRepository.delete(existingFriendship);
            } else if ("PENDING".equals(status)) {
                // BR-Block-02: Hủy lời mời kết bạn đang PENDING từ bất kỳ phía nào
                friendshipRepository.delete(existingFriendship);
            }
            // Status REJECTED: không cần xử lý gì thêm
        }

        return "Đã chặn người dùng @" + blocked.getUsername() + " thành công.";
    }

    // ============================================================
    // LUỒNG 2: Bỏ chặn người dùng
    // Endpoint: DELETE /api/v1/blocks/{targetUserId}
    // ============================================================

    /**
     * Gỡ bỏ lệnh chặn đối với một người dùng.
     *
     * Lưu ý nghiệp vụ: Unblock KHÔNG tự động kết bạn lại.
     * Sau khi unblock, hai người trở lại trạng thái "người lạ" (stranger).
     * Muốn kết bạn lại, phải gửi lời mời mới qua /friendships/request.
     *
     * @param blockerUsername Username của người thực hiện unblock
     * @param targetUserId    ID của người bị gỡ chặn
     * @return Thông báo kết quả
     */
    @Transactional
    public String unblockUser(String blockerUsername, Long targetUserId) {
        User blocker = userRepository.findByUsername(blockerUsername)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + blockerUsername));
        User blocked = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user ID: " + targetUserId));

        // Tìm bản ghi block — nếu không tồn tại nghĩa là chưa block bao giờ
        UserBlock block = userBlockRepository.findByBlockerAndBlocked(blocker, blocked)
                .orElseThrow(() -> new IllegalStateException("Bạn chưa chặn người dùng này."));

        userBlockRepository.delete(block);
        return "Đã bỏ chặn người dùng @" + blocked.getUsername() + ".";
    }

    // ============================================================
    // LUỒNG 3: Lấy danh sách người đang bị chặn
    // Endpoint: GET /api/v1/blocks
    // ============================================================

    /**
     * Lấy danh sách username của tất cả người mà user hiện tại đang chặn.
     *
     * Trả về List<String> (username) thay vì List<UserBlock> để tránh expose
     * thông tin nội bộ (created_at, id) — an toàn hơn cho API response.
     *
     * @param currentUsername Username của người đang đăng nhập
     * @return Danh sách username bị block
     */
    public List<String> getBlockedUsers(String currentUsername) {
        User blocker = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + currentUsername));

        return userBlockRepository.findAllByBlocker(blocker)
                .stream()
                // Lấy username của người bị block từ mỗi UserBlock record
                .map(block -> block.getBlocked().getUsername())
                .collect(Collectors.toList());
    }

    // ============================================================
    // UTILITY METHODS — dùng bởi PostService và FriendshipService
    // ============================================================

    /**
     * Kiểm tra xem có tồn tại block theo BẤT KỲ chiều nào giữa hai user không.
     * Đây là method được inject vào PostService và FriendshipService.
     *
     * Ví dụ sử dụng trong PostService.canUserViewPost():
     *   if (userBlockService.isBlockedBetween(viewer, author)) return false;
     *
     * @param u1 User thứ nhất
     * @param u2 User thứ hai
     * @return true nếu tồn tại block theo bất kỳ chiều nào
     */
    public boolean isBlockedBetween(User u1, User u2) {
        // Gọi query kiểm tra cả hai chiều (u1 block u2 OR u2 block u1)
        return userBlockRepository.existsBlockBetween(u1, u2);
    }

    /**
     * Lấy danh sách ID của những người mà user này đã block.
     * Được dùng trong PostService.getAllPosts() để filter feed hiệu quả.
     *
     * Cách dùng điển hình trong PostService:
     *   List<Long> blockedIds = userBlockService.getBlockedUserIds(viewer);
     *   posts.removeIf(p -> blockedIds.contains(p.getAuthor().getId()));
     *
     * @param user User cần lấy danh sách block
     * @return Set<Long> ID của những người bị block (dùng Set để contains() O(1))
     */
    public java.util.Set<Long> getBlockedUserIds(User user) {
        return new java.util.HashSet<>(userBlockRepository.findBlockedUserIdsByBlocker(user));
    }
}
