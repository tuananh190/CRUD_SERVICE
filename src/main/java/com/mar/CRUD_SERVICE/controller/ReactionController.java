package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.model.ReactionType;
import com.mar.CRUD_SERVICE.service.ReactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/reactions")
public class ReactionController {

    private final ReactionService reactionService;

    public ReactionController(ReactionService reactionService) {
        this.reactionService = reactionService;
    }

    // React (LIKE / ANGRY) trên bài viết
    @PostMapping("/post/{postId}")
    public ResponseEntity<String> reactToPost(@PathVariable Long postId,
                                              @RequestParam("type") ReactionType type,
                                              Principal principal) {
        try {
            String result = reactionService.reactToPost(postId, principal.getName(), type);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // React (LIKE / ANGRY) trên comment
    @PostMapping("/comment/{commentId}")
    public ResponseEntity<String> reactToComment(@PathVariable Long commentId,
                                                 @RequestParam("type") ReactionType type,
                                                 Principal principal) {
        try {
            String result = reactionService.reactToComment(commentId, principal.getName(), type);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Legacy API 21: Like bài viết (mapped to LIKE reaction)
    @PostMapping("/post/{postId}/like")
    public ResponseEntity<String> likePost(@PathVariable Long postId, Principal principal) {
        try {
            String result = reactionService.reactToPost(postId, principal.getName(), ReactionType.LIKE);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Legacy API 22: Unlike bài viết (mapped to LIKE reaction removal)
    @DeleteMapping("/post/{postId}/like")
    public ResponseEntity<String> unlikePost(@PathVariable Long postId, Principal principal) {
        try {
            String result = reactionService.reactToPost(postId, principal.getName(), ReactionType.LIKE);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Legacy API 23: Like comment
    @PostMapping("/comment/{commentId}/like")
    public ResponseEntity<String> likeComment(@PathVariable Long commentId, Principal principal) {
        try {
            String result = reactionService.reactToComment(commentId, principal.getName(), ReactionType.LIKE);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Legacy API 24: Unlike comment
    @DeleteMapping("/comment/{commentId}/like")
    public ResponseEntity<String> unlikeComment(@PathVariable Long commentId, Principal principal) {
        try {
            String result = reactionService.reactToComment(commentId, principal.getName(), ReactionType.LIKE);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

