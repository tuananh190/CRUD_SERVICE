package com.mar.CRUD_SERVICE.service;

import com.mar.CRUD_SERVICE.model.Follow;
import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.repository.FollowRepository;
import com.mar.CRUD_SERVICE.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public FollowService(FollowRepository followRepository, UserRepository userRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
    }

    public String follow(Long targetUserId, String currentUsername) {
        User follower = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + currentUsername));
        User following = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user ID: " + targetUserId));

        if (follower.getId().equals(following.getId())) {
            throw new IllegalStateException("Bạn không thể tự follow chính mình.");
        }
        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new IllegalStateException("Bạn đã follow người dùng này rồi.");
        }

        followRepository.save(new Follow(follower, following, LocalDateTime.now()));
        return "Đã follow @" + following.getUsername() + " thành công!";
    }

    public String unfollow(Long targetUserId, String currentUsername) {
        User follower = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + currentUsername));
        User following = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user ID: " + targetUserId));

        Follow follow = followRepository.findByFollowerAndFollowing(follower, following)
                .orElseThrow(() -> new IllegalStateException("Bạn chưa follow người dùng này."));
        followRepository.delete(follow);
        return "Đã unfollow @" + following.getUsername();
    }

    public List<String> getFollowers(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user ID: " + userId));
        return followRepository.findByFollowing(user).stream()
                .map(f -> f.getFollower().getUsername())
                .collect(Collectors.toList());
    }

    public List<String> getFollowing(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user ID: " + userId));
        return followRepository.findByFollower(user).stream()
                .map(f -> f.getFollowing().getUsername())
                .collect(Collectors.toList());
    }
}
