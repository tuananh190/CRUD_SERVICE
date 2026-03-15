package com.mar.CRUD_SERVICE.service;

import com.mar.CRUD_SERVICE.model.Friendship;
import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.repository.FriendshipRepository;
import com.mar.CRUD_SERVICE.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public FriendshipService(FriendshipRepository friendshipRepository,
                             UserRepository userRepository,
                             NotificationService notificationService) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    // Gửi lời mời kết bạn
    public String sendFriendRequest(String currentUsername, Long targetUserId) {
        User sender = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + currentUsername));
        User receiver = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user ID: " + targetUserId));

        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalStateException("Bạn không thể gửi lời mời kết bạn cho chính mình.");
        }

        // kiểm tra đã có quan hệ trước đó hay chưa
        Friendship existing = friendshipRepository.findByUsers(sender, receiver).orElse(null);
        if (existing != null) {
            if ("PENDING".equals(existing.getStatus())) {
                throw new IllegalStateException("Đã tồn tại lời mời kết bạn đang chờ xử lý giữa hai người.");
            }
            if ("ACCEPTED".equals(existing.getStatus())) {
                throw new IllegalStateException("Hai người đã là bạn bè.");
            }
            // BR-21: Allow re-sending after REJECTED status
            if ("REJECTED".equals(existing.getStatus())) {
                existing.setStatus("PENDING");
                existing.setCreatedAt(LocalDateTime.now());
                friendshipRepository.save(existing);
                notificationService.createNotification(
                        receiver,
                        "Bạn nhận được lời mời kết bạn từ @" + sender.getUsername()
                );
                return "Đã gửi lại lời mời kết bạn tới @" + receiver.getUsername();
            }
        }

        Friendship friendship = new Friendship(sender, receiver, "PENDING", LocalDateTime.now());
        friendshipRepository.save(friendship);

        // thông báo cho người nhận
        notificationService.createNotification(
                receiver,
                "Bạn nhận được lời mời kết bạn từ @" + sender.getUsername()
        );

        return "Đã gửi lời mời kết bạn tới @" + receiver.getUsername();
    }

    // Chấp nhận lời mời kết bạn
    public String acceptFriendRequest(String currentUsername, Long friendshipId) {
        User current = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + currentUsername));

        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lời mời kết bạn ID: " + friendshipId));

        if (!"PENDING".equals(friendship.getStatus())) {
            throw new IllegalStateException("Lời mời kết bạn này không còn ở trạng thái PENDING.");
        }

        if (!friendship.getUser2().getId().equals(current.getId())) {
            throw new IllegalStateException("Bạn không có quyền chấp nhận lời mời kết bạn này.");
        }

        friendship.setStatus("ACCEPTED");
        friendshipRepository.save(friendship);

        // thông báo cho người gửi
        notificationService.createNotification(
                friendship.getUser1(),
                "Lời mời kết bạn của bạn đã được @" + current.getUsername() + " chấp nhận."
        );

        return "Đã chấp nhận lời mời kết bạn.";
    }

    // Từ chối lời mời kết bạn
    public String declineFriendRequest(String currentUsername, Long friendshipId) {
        User current = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + currentUsername));

        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lời mời kết bạn ID: " + friendshipId));

        if (!"PENDING".equals(friendship.getStatus())) {
            throw new IllegalStateException("Lời mời kết bạn này không còn ở trạng thái PENDING.");
        }

        if (!friendship.getUser2().getId().equals(current.getId())) {
            throw new IllegalStateException("Bạn không có quyền từ chối lời mời kết bạn này.");
        }

        // BR-21: Set status to REJECTED instead of deleting (maintains history)
        friendship.setStatus("REJECTED");
        friendshipRepository.save(friendship);

        // thông báo cho người gửi
        notificationService.createNotification(
                friendship.getUser1(),
                "Lời mời kết bạn của bạn đã bị @" + current.getUsername() + " từ chối."
        );

        return "Đã từ chối lời mời kết bạn.";
    }

    // Huỷ kết bạn (unfriend)
    public String unfriend(String currentUsername, Long targetUserId) {
        User current = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + currentUsername));
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user ID: " + targetUserId));

        Friendship friendship = friendshipRepository.findByUsers(current, target)
                .orElseThrow(() -> new IllegalStateException("Hai người chưa phải là bạn bè hoặc không có quan hệ kết bạn."));

        if (!"ACCEPTED".equals(friendship.getStatus())) {
            throw new IllegalStateException("Chỉ có thể huỷ kết bạn khi đang ở trạng thái ACCEPTED. Trạng thái hiện tại: " + friendship.getStatus());
        }

        friendshipRepository.delete(friendship);
        return "Đã huỷ kết bạn với @" + target.getUsername();
    }

    // Lấy danh sách lời mời kết bạn đang chờ xử lý cho current user (là người nhận)
    public List<String> getPendingRequests(String currentUsername) {
        User current = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + currentUsername));

        List<Friendship> pendings = friendshipRepository.findByUser2AndStatus(current, "PENDING");
        return pendings.stream()
                .map(f -> f.getUser1().getUsername())
                .collect(Collectors.toList());
    }

    // Lấy danh sách bạn bè của một user
    public List<String> getFriends(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user ID: " + userId));

        List<Friendship> accepted = friendshipRepository.findAcceptedFriendshipsByUser(user);
        return accepted.stream()
                .map(f -> f.getUser1().equals(user) ? f.getUser2().getUsername() : f.getUser1().getUsername())
                .collect(Collectors.toList());
    }
}

