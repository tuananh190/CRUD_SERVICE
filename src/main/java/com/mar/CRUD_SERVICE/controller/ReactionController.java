package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.dto.response.ReactionResponse;
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

    @PostMapping("/post/{postId}")
    public ResponseEntity<ReactionResponse> reactToPost(@PathVariable Long postId,
                                                        @RequestParam("type") ReactionType type,
                                                        Principal principal) {
        try {
            ReactionResponse result = reactionService.reactToPost(postId, principal.getName(), type);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(null);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/comment/{commentId}")
    public ResponseEntity<ReactionResponse> reactToComment(@PathVariable Long commentId,
                                                           @RequestParam("type") ReactionType type,
                                                           Principal principal) {
        try {
            ReactionResponse result = reactionService.reactToComment(commentId, principal.getName(), type);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(null);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Deprecated
    @PostMapping("/post/{postId}/like")
    public ResponseEntity<ReactionResponse> likePost(@PathVariable Long postId, Principal principal) {

        return reactToPost(postId, ReactionType.LIKE, principal);
    }

    @Deprecated
    @DeleteMapping("/post/{postId}/like")
    public ResponseEntity<ReactionResponse> unlikePost(@PathVariable Long postId, Principal principal) {

        return reactToPost(postId, ReactionType.LIKE, principal);
    }

    @Deprecated
    @PostMapping("/comment/{commentId}/like")
    public ResponseEntity<ReactionResponse> likeComment(@PathVariable Long commentId, Principal principal) {
        return reactToComment(commentId, ReactionType.LIKE, principal);
    }

    @Deprecated
    @DeleteMapping("/comment/{commentId}/like")
    public ResponseEntity<ReactionResponse> unlikeComment(@PathVariable Long commentId, Principal principal) {
        return reactToComment(commentId, ReactionType.LIKE, principal);
    }
}
