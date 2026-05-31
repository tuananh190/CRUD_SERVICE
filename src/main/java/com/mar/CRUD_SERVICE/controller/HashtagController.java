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
    public ResponseEntity<List<Hashtag>> getAllHashtags() {
        return ResponseEntity.ok(hashtagService.getAllHashtags());
    }

    @GetMapping("/{tag}/posts")
    public ResponseEntity<?> getPostsByHashtag(@PathVariable String tag) {
        try {
            List<Post> posts = hashtagService.getPostsByHashtag(tag);
            return ResponseEntity.ok(posts);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/post/{postId}")
    public ResponseEntity<ApiResponse<String>> addHashtagToPost(
            @PathVariable Long postId,
            @RequestParam String tag) {
        try {
            return ResponseEntity.ok(new ApiResponse<>(200, hashtagService.addHashtagToPost(postId, tag), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, e.getMessage(), null));
        }
    }

    @DeleteMapping("/post/{postId}/{tag}")
    public ResponseEntity<ApiResponse<String>> removeHashtagFromPost(
            @PathVariable Long postId,
            @PathVariable String tag) {
        try {
            return ResponseEntity.ok(new ApiResponse<>(200, hashtagService.removeHashtagFromPost(postId, tag), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(400, e.getMessage(), null));
        }
    }
}
