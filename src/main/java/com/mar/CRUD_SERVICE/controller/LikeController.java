package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.service.LikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * @deprecated Use {@link ReactionController} instead.
 * This controller is maintained for backward compatibility only.
 * All like functionality has been consolidated into ReactionController
 * using ReactionType.LIKE for unified architecture.
 *
 * New implementations should use:
 * POST /api/v1/reactions/post/{postId}?type=LIKE
 * POST /api/v1/reactions/comment/{commentId}?type=LIKE
 */
@Deprecated(since = "1.1", forRemoval = true)
@RestController
@RequestMapping("/api/v1/likes")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    // ...existing code...
    @PostMapping("/post/{postId}")
    public ResponseEntity<String> likePost(@PathVariable Long postId, Principal principal) {
        try {
            String result = likeService.likePost(postId, principal.getName());
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // API 22: Unlike bài viết
    @DeleteMapping("/post/{postId}")
    public ResponseEntity<String> unlikePost(@PathVariable Long postId, Principal principal) {
        try {
            String result = likeService.unlikePost(postId, principal.getName());
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // API 23: Like comment
    @PostMapping("/comment/{commentId}")
    public ResponseEntity<String> likeComment(@PathVariable Long commentId, Principal principal) {
        try {
            String result = likeService.likeComment(commentId, principal.getName());
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // API 24: Unlike comment
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<String> unlikeComment(@PathVariable Long commentId, Principal principal) {
        try {
            String result = likeService.unlikeComment(commentId, principal.getName());
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
