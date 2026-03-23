package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.service.FriendshipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/friendships")
public class FriendshipController {

    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    // Gửi lời mời kết bạn
    @PostMapping("/request/{targetUserId}")
    public ResponseEntity<String> sendFriendRequest(@PathVariable Long targetUserId,
                                                    Principal principal) {
        try {
            String result = friendshipService.sendFriendRequest(principal.getName(), targetUserId);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Chấp nhận lời mời kết bạn
    @PostMapping("/accept/{friendshipId}")
    public ResponseEntity<String> acceptFriendRequest(@PathVariable Long friendshipId,
                                                      Principal principal) {
        try {
            String result = friendshipService.acceptFriendRequest(principal.getName(), friendshipId);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Từ chối lời mời kết bạn
    @PostMapping("/decline/{friendshipId}")
    public ResponseEntity<String> declineFriendRequest(@PathVariable Long friendshipId,
                                                       Principal principal) {
        try {
            String result = friendshipService.declineFriendRequest(principal.getName(), friendshipId);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Huỷ kết bạn
    @DeleteMapping("/unfriend/{targetUserId}")
    public ResponseEntity<String> unfriend(@PathVariable Long targetUserId,
                                           Principal principal) {
        try {
            String result = friendshipService.unfriend(principal.getName(), targetUserId);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Lấy danh sách lời mời kết bạn đang chờ xử lý (current user là người nhận)
    @GetMapping("/pending")
    public ResponseEntity<List<String>> getPendingRequests(Principal principal) {
        List<String> usernames = friendshipService.getPendingRequests(principal.getName());
        return ResponseEntity.ok(usernames);
    }

    // Lấy danh sách bạn bè của một user
    @GetMapping("/friends/{userId}")
    public ResponseEntity<List<String>> getFriends(@PathVariable Long userId) {
        try {
            List<String> friends = friendshipService.getFriends(userId);
            return ResponseEntity.ok(friends);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

