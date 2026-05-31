package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.model.Post;
import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.service.PostService;
import com.mar.CRUD_SERVICE.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final UserService userService;
    private final PostService postService;

    public SearchController(UserService userService, PostService postService) {
        this.userService = userService;
        this.postService = postService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String q) {
        return ResponseEntity.ok(userService.searchUsers(q));
    }

    @GetMapping("/posts")
    public ResponseEntity<List<Post>> searchPosts(@RequestParam String q) {
        return ResponseEntity.ok(postService.searchPosts(q));
    }
}
