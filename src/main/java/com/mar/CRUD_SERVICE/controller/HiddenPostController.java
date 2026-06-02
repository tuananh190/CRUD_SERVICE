package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.service.HiddenPostService;
import com.mar.CRUD_SERVICE.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/posts")
public class HiddenPostController {

    private final HiddenPostService hiddenPostService;

    public HiddenPostController(HiddenPostService hiddenPostService) {
        this.hiddenPostService = hiddenPostService;
    }

    @PostMapping("/{postId}/hide")
    public ResponseEntity<ApiResponse<String>> hidePost(@PathVariable Long postId, Principal principal) {
        String result = hiddenPostService.hidePost(principal.getName(), postId);
        return ResponseEntity.ok(new ApiResponse<>(200, result, null));
    }

    @DeleteMapping("/{postId}/hide")
    public ResponseEntity<ApiResponse<String>> unhidePost(@PathVariable Long postId, Principal principal) {
        String result = hiddenPostService.unhidePost(principal.getName(), postId);
        return ResponseEntity.ok(new ApiResponse<>(200, result, null));
    }
}
