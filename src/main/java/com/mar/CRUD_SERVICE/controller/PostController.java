package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.dto.request.PostCreationRequest;
import com.mar.CRUD_SERVICE.dto.response.PostResponse;
import com.mar.CRUD_SERVICE.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    // API 12: Lấy danh sách tất cả bài viết
    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    // API 13: Lấy chi tiết bài viết theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable Long id) {
        PostResponse resp = postService.getPostById(id);
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
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
