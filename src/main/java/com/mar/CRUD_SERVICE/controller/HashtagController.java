package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.model.Hashtag;
import com.mar.CRUD_SERVICE.model.Post;
import com.mar.CRUD_SERVICE.service.HashtagService;
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

    // API 37: Lấy danh sách tất cả hashtag
    @GetMapping
    public ResponseEntity<List<Hashtag>> getAllHashtags() {
        return ResponseEntity.ok(hashtagService.getAllHashtags());
    }

    // API 38: Lấy tất cả bài viết có hashtag đó
    @GetMapping("/{tag}/posts")
    public ResponseEntity<?> getPostsByHashtag(@PathVariable String tag) {
        try {
            List<Post> posts = hashtagService.getPostsByHashtag(tag);
            return ResponseEntity.ok(posts);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // API 39: Thêm hashtag vào bài viết
    @PostMapping("/post/{postId}")
    public ResponseEntity<String> addHashtagToPost(
            @PathVariable Long postId,
            @RequestParam String tag) {
        try {
            return ResponseEntity.ok(hashtagService.addHashtagToPost(postId, tag));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // API 40: Xoá hashtag khỏi bài viết
    @DeleteMapping("/post/{postId}/{tag}")
    public ResponseEntity<String> removeHashtagFromPost(
            @PathVariable Long postId,
            @PathVariable String tag) {
        try {
            return ResponseEntity.ok(hashtagService.removeHashtagFromPost(postId, tag));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
