package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.dto.request.PostCreationRequest;
import com.mar.CRUD_SERVICE.dto.request.ShareRequest;
import com.mar.CRUD_SERVICE.dto.response.PostResponse;
import com.mar.CRUD_SERVICE.dto.response.PostListResponse;
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

    // API 11: Tạo bài viết mới
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody PostCreationRequest request) {
        try {
            PostResponse resp = postService.createPost(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    // API: Share/Repost một bài viết hiện có
    @PostMapping("/{id}/share")
    public ResponseEntity<?> sharePost(@PathVariable Long id, @RequestBody(required = false) ShareRequest request) {
        try {
            // Lấy thông tin người đăng share
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

    // API 12: Lấy danh sách bài viết (ưu tiên theo sở thích người dùng nếu đã đăng nhập)
    @GetMapping
    public ResponseEntity<List<PostListResponse>> getAllPosts(Principal principal) {
        String username = (principal != null) ? principal.getName() : null;
        return ResponseEntity.ok(postService.getAllPosts(username));
    }

    // API 13: Lấy chi tiết bài viết theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable Long id, Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        PostResponse resp = postService.getPostById(id, currentUsername);
        if (resp == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(resp);
    }

    // API 14: Cập nhật nội dung bài viết theo ID
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

    // API 15: Xóa bài viết theo ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        try {
            postService.deletePost(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }
}
