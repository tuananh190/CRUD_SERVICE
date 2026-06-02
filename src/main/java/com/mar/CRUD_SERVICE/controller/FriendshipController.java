package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.service.FriendshipService;
import com.mar.CRUD_SERVICE.dto.response.ApiResponse;
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

    @PostMapping("/request/{targetUserId}")
    public ResponseEntity<ApiResponse<String>> sendFriendRequest(@PathVariable Long targetUserId, Principal principal) {
        String result = friendshipService.sendFriendRequest(principal.getName(), targetUserId);
        return ResponseEntity.ok(new ApiResponse<>(200, result, null));
    }

    @PostMapping("/accept/{friendshipId}")
    public ResponseEntity<ApiResponse<String>> acceptFriendRequest(@PathVariable Long friendshipId, Principal principal) {
        String result = friendshipService.acceptFriendRequest(principal.getName(), friendshipId);
        return ResponseEntity.ok(new ApiResponse<>(200, result, null));
    }

    @PostMapping("/decline/{friendshipId}")
    public ResponseEntity<ApiResponse<String>> declineFriendRequest(@PathVariable Long friendshipId, Principal principal) {
        String result = friendshipService.declineFriendRequest(principal.getName(), friendshipId);
        return ResponseEntity.ok(new ApiResponse<>(200, result, null));
    }

    @DeleteMapping("/unfriend/{targetUserId}")
    public ResponseEntity<ApiResponse<String>> unfriend(@PathVariable Long targetUserId, Principal principal) {
        String result = friendshipService.unfriend(principal.getName(), targetUserId);
        return ResponseEntity.ok(new ApiResponse<>(200, result, null));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<String>>> getPendingRequests(Principal principal) {
        List<String> usernames = friendshipService.getPendingRequests(principal.getName());
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy danh sách thành công", usernames));
    }

    @GetMapping("/friends/{userId}")
    public ResponseEntity<ApiResponse<List<String>>> getFriends(@PathVariable Long userId) {
        List<String> friends = friendshipService.getFriends(userId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy danh sách thành công", friends));
    }
}
