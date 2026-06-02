package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.dto.request.CommentCreationRequest;
import com.mar.CRUD_SERVICE.dto.response.CommentResponse;
import com.mar.CRUD_SERVICE.dto.response.ApiResponse;
import com.mar.CRUD_SERVICE.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {
    private CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(@RequestBody CommentCreationRequest request) {
        CommentResponse resp = commentService.createComment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(201, "Tạo bình luận thành công", resp));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getAllComments() {
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy danh sách bình luận thành công", commentService.getAllComments()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CommentResponse>> getCommentById(@PathVariable Long id) {
        CommentResponse resp = commentService.getCommentById(id);
        if (resp == null) throw new com.mar.CRUD_SERVICE.exception.ResourceNotFoundException("Không tìm thấy bình luận");
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy thông tin bình luận thành công", resp));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(@PathVariable Long id, @RequestBody CommentCreationRequest request) {
        CommentResponse updated = commentService.updateComment(id, request);
        if (updated == null) throw new com.mar.CRUD_SERVICE.exception.ResourceNotFoundException("Không tìm thấy bình luận");
        return ResponseEntity.ok(new ApiResponse<>(200, "Cập nhật bình luận thành công", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Xóa bình luận thành công", null));
    }
}
