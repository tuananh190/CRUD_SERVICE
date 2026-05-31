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

@Service
public class UserBlockService {

    private final UserBlockRepository userBlockRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    public UserBlockService(UserBlockRepository userBlockRepository,
                            UserRepository userRepository,
                            FriendshipRepository friendshipRepository) {
        this.userBlockRepository = userBlockRepository;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    @Transactional
    public String blockUser(String blockerUsername, Long targetUserId) {

        User blocker = userRepository.findByUsername(blockerUsername)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + blockerUsername));

        User blocked = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user ID: " + targetUserId));

        if (blocker.getId().equals(blocked.getId())) {
            throw new IllegalStateException("Bạn không thể tự chặn chính mình.");
        }

        if (userBlockRepository.existsByBlockerAndBlocked(blocker, blocked)) {
            throw new IllegalStateException("Bạn đã chặn người dùng này rồi.");
        }

        UserBlock newBlock = new UserBlock(blocker, blocked);
        userBlockRepository.save(newBlock);

        List<Friendship> existingRelations = friendshipRepository.findByUsers(blocker, blocked);
        if (!existingRelations.isEmpty()) {
            Friendship existingFriendship = existingRelations.get(0);
            String status = existingFriendship.getStatus();

            if ("ACCEPTED".equals(status)) {

                friendshipRepository.delete(existingFriendship);
            } else if ("PENDING".equals(status)) {

                friendshipRepository.delete(existingFriendship);
            }

        }

        return "Đã chặn người dùng @" + blocked.getUsername() + " thành công.";
    }

    @Transactional
    public String unblockUser(String blockerUsername, Long targetUserId) {
        User blocker = userRepository.findByUsername(blockerUsername)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + blockerUsername));
        User blocked = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user ID: " + targetUserId));

        UserBlock block = userBlockRepository.findByBlockerAndBlocked(blocker, blocked)
                .orElseThrow(() -> new IllegalStateException("Bạn chưa chặn người dùng này."));

        userBlockRepository.delete(block);
        return "Đã bỏ chặn người dùng @" + blocked.getUsername() + ".";
    }

    public List<String> getBlockedUsers(String currentUsername) {
        User blocker = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + currentUsername));

        return userBlockRepository.findAllByBlocker(blocker)
                .stream()

                .map(block -> block.getBlocked().getUsername())
                .collect(Collectors.toList());
    }

    public boolean isBlockedBetween(User u1, User u2) {

        return userBlockRepository.existsBlockBetween(u1, u2);
    }

    public java.util.Set<Long> getBlockedUserIds(User user) {
        return new java.util.HashSet<>(userBlockRepository.findBlockedUserIdsByBlocker(user));
    }
}
