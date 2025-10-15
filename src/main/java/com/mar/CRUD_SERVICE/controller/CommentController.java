package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.dto.request.CommentCreationRequest;
import com.mar.CRUD_SERVICE.dto.response.CommentResponse;
import com.mar.CRUD_SERVICE.service.CommentService;
import com.mar.CRUD_SERVICE.model.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(@RequestBody CommentCreationRequest request) {
        return ResponseEntity.ok(commentService.createComment(request));
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getAllComments() {
        return ResponseEntity.ok(commentService.getAllComments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentResponse> getCommentById(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.getCommentById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentResponse> updateComment(@PathVariable Long id, @RequestBody CommentCreationRequest request) {
        return ResponseEntity.ok(commentService.updateComment(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
