package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.dto.response.ApiResponse;

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

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<List<PostListResponse>>> getRecommendedPosts(
            Principal principal,
            @RequestParam(name = "limit", defaultValue = "20") int limit) {
        List<PostListResponse> posts = userInterestService.getRecommendedPosts(principal.getName(), limit);
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy danh sách bài viết gợi ý thành công", posts));
    }
}
