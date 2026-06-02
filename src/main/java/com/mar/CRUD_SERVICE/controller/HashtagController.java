package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.model.Hashtag;
import com.mar.CRUD_SERVICE.model.Post;
import com.mar.CRUD_SERVICE.service.HashtagService;
import com.mar.CRUD_SERVICE.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hashtags")
public class HashtagController {

    private final HashtagService hashtagService;

    public HashtagController(HashtagService hashtagService) {
        this.hashtagService = hashtagService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Hashtag>>> getAllHashtags() {
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy danh sách thành công", hashtagService.getAllHashtags()));
    }

    @GetMapping("/{tag}/posts")
    public ResponseEntity<ApiResponse<List<Post>>> getPostsByHashtag(@PathVariable String tag) {
        List<Post> posts = hashtagService.getPostsByHashtag(tag);
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy danh sách bài viết thành công", posts));
    }

    @PostMapping("/post/{postId}")
    public ResponseEntity<ApiResponse<String>> addHashtagToPost(
            @PathVariable Long postId,
            @RequestParam String tag) {
        return ResponseEntity.ok(new ApiResponse<>(200, hashtagService.addHashtagToPost(postId, tag), null));
    }

    @DeleteMapping("/post/{postId}/{tag}")
    public ResponseEntity<ApiResponse<String>> removeHashtagFromPost(
            @PathVariable Long postId,
            @PathVariable String tag) {
        return ResponseEntity.ok(new ApiResponse<>(200, hashtagService.removeHashtagFromPost(postId, tag), null));
    }
}
