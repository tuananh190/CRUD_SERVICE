package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.dto.response.ApiResponse;

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
    public ResponseEntity<ApiResponse<ReactionResponse>> reactToPost(@PathVariable Long postId,
                                                        @RequestParam("type") ReactionType type,
                                                        Principal principal) {
        ReactionResponse result = reactionService.reactToPost(postId, principal.getName(), type);
        return ResponseEntity.ok(new ApiResponse<>(200, "Thả cảm xúc thành công", result));
    }

    @PostMapping("/comment/{commentId}")
    public ResponseEntity<ApiResponse<ReactionResponse>> reactToComment(@PathVariable Long commentId,
                                                           @RequestParam("type") ReactionType type,
                                                           Principal principal) {
        ReactionResponse result = reactionService.reactToComment(commentId, principal.getName(), type);
        return ResponseEntity.ok(new ApiResponse<>(200, "Thả cảm xúc thành công", result));
    }

    @Deprecated
    @PostMapping("/post/{postId}/like")
    public ResponseEntity<ApiResponse<ReactionResponse>> likePost(@PathVariable Long postId, Principal principal) {
        return reactToPost(postId, ReactionType.LIKE, principal);
    }

    @Deprecated
    @DeleteMapping("/post/{postId}/like")
    public ResponseEntity<ApiResponse<ReactionResponse>> unlikePost(@PathVariable Long postId, Principal principal) {
        return reactToPost(postId, ReactionType.LIKE, principal);
    }

    @Deprecated
    @PostMapping("/comment/{commentId}/like")
    public ResponseEntity<ApiResponse<ReactionResponse>> likeComment(@PathVariable Long commentId, Principal principal) {
        return reactToComment(commentId, ReactionType.LIKE, principal);
    }

    @Deprecated
    @DeleteMapping("/comment/{commentId}/like")
    public ResponseEntity<ApiResponse<ReactionResponse>> unlikeComment(@PathVariable Long commentId, Principal principal) {
        return reactToComment(commentId, ReactionType.LIKE, principal);
    }
}
