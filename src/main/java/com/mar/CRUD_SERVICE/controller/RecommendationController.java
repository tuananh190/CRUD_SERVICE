package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.dto.response.PostListResponse;
import com.mar.CRUD_SERVICE.service.UserInterestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    private final UserInterestService userInterestService;

    public RecommendationController(UserInterestService userInterestService) {
        this.userInterestService = userInterestService;
    }

    // Gợi ý bài viết theo sở thích của user hiện tại
    @GetMapping("/posts")
    public ResponseEntity<List<PostListResponse>> getRecommendedPosts(
            Principal principal,
            @RequestParam(name = "limit", defaultValue = "20") int limit) {
        List<PostListResponse> posts = userInterestService.getRecommendedPosts(principal.getName(), limit);
        return ResponseEntity.ok(posts);
    }
}

