package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.dto.request.PostCreationRequest;
import com.mar.CRUD_SERVICE.dto.request.ShareRequest;
import com.mar.CRUD_SERVICE.dto.response.PostResponse;
import com.mar.CRUD_SERVICE.dto.response.PostListResponse;
import com.mar.CRUD_SERVICE.dto.response.ApiResponse;
import com.mar.CRUD_SERVICE.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {
    private PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody PostCreationRequest request) {
        try {
            PostResponse resp = postService.createPost(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<?> sharePost(@PathVariable Long id, @RequestBody(required = false) ShareRequest request) {
        try {

            String currentUsername = null;
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            }
            if (currentUsername == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Người dùng chưa đăng nhập.");
            }

            PostResponse resp = postService.sharePost(id, request, currentUsername);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (IllegalStateException | IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @GetMapping("/trending")
    public ResponseEntity<List<PostListResponse>> getTrendingPosts(
            @RequestParam(name = "limit", defaultValue = "10") int limit) {
        return ResponseEntity.ok(postService.getTrendingPosts(limit));
    }

    @GetMapping
    public ResponseEntity<List<PostListResponse>> getAllPosts(Principal principal) {
        String username = (principal != null) ? principal.getName() : null;
        return ResponseEntity.ok(postService.getAllPosts(username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable Long id, Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        PostResponse resp = postService.getPostById(id, currentUsername);
        if (resp == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Long id, @RequestBody PostCreationRequest request) {
        try {
            PostResponse updated = postService.updatePost(id, request);
            if (updated == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        try {
            postService.deletePost(id);
            return ResponseEntity.ok(new ApiResponse<>(200, "Xóa bài viết thành công", null));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }
}
