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
    public ResponseEntity<ApiResponse<PostResponse>> createPost(@RequestBody PostCreationRequest request) {
        PostResponse resp = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(201, "Tạo bài viết thành công", resp));
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<ApiResponse<PostResponse>> sharePost(@PathVariable Long id, @RequestBody(required = false) ShareRequest request) {
        String currentUsername = null;
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        }
        if (currentUsername == null) {
            throw new com.mar.CRUD_SERVICE.exception.UnauthorizedException("Người dùng chưa đăng nhập.");
        }

        PostResponse resp = postService.sharePost(id, request, currentUsername);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(201, "Chia sẻ bài viết thành công", resp));
    }

    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<List<PostListResponse>>> getTrendingPosts(
            @RequestParam(name = "limit", defaultValue = "10") int limit) {
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy danh sách bài viết thịnh hành thành công", postService.getTrendingPosts(limit)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostListResponse>>> getAllPosts(Principal principal) {
        String username = (principal != null) ? principal.getName() : null;
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy danh sách bài viết thành công", postService.getAllPosts(username)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> getPostById(@PathVariable Long id, Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        PostResponse resp = postService.getPostById(id, currentUsername);
        if (resp == null) {
            throw new com.mar.CRUD_SERVICE.exception.ResourceNotFoundException("Không tìm thấy bài viết");
        }
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy thông tin bài viết thành công", resp));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(@PathVariable Long id, @RequestBody PostCreationRequest request) {
        PostResponse updated = postService.updatePost(id, request);
        if (updated == null) throw new com.mar.CRUD_SERVICE.exception.ResourceNotFoundException("Không tìm thấy bài viết");
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật bài viết thành công", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Xóa bài viết thành công", null));
    }
}
