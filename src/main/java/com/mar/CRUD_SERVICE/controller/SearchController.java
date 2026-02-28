package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.model.Post;
import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.repository.PostRepository;
import com.mar.CRUD_SERVICE.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public SearchController(UserRepository userRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    // API 33: Tìm kiếm người dùng theo tên/username
    @GetMapping("/users")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String q) {
        List<User> users = userRepository
                .findByUsernameContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(q, q, q);
        return ResponseEntity.ok(users);
    }

    // API 34: Tìm kiếm bài viết theo tiêu đề hoặc nội dung
    @GetMapping("/posts")
    public ResponseEntity<List<Post>> searchPosts(@RequestParam String q) {
        List<Post> posts = postRepository
                .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(q, q);
        return ResponseEntity.ok(posts);
    }
}
