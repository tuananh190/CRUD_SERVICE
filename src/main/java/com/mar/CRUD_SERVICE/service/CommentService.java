package com.mar.CRUD_SERVICE.service;

import com.mar.CRUD_SERVICE.dto.request.CommentCreationRequest;
import com.mar.CRUD_SERVICE.dto.response.CommentResponse;
import java.util.List;

public interface CommentService {
    CommentResponse createComment(CommentCreationRequest request);
    List<CommentResponse> getAllComments();
    CommentResponse getCommentById(Long id);
    CommentResponse updateComment(Long id, CommentCreationRequest request);
    void deleteComment(Long id);
}

