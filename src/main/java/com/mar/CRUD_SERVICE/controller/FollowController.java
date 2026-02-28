package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.service.FollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/follows")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    // API 25: Follow người dùng
    @PostMapping("/{userId}")
    public ResponseEntity<String> follow(@PathVariable Long userId, Principal principal) {
        try {
            String result = followService.follow(userId, principal.getName());
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // API 26: Unfollow người dùng
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> unfollow(@PathVariable Long userId, Principal principal) {
        try {
            String result = followService.unfollow(userId, principal.getName());
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // API 27: Lấy danh sách followers
    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<String>> getFollowers(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(followService.getFollowers(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // API 28: Lấy danh sách đang follow
    @GetMapping("/{userId}/following")
    public ResponseEntity<List<String>> getFollowing(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(followService.getFollowing(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
