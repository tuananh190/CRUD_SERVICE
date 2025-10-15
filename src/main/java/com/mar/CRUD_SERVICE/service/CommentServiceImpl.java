package com.mar.CRUD_SERVICE.service;

import com.mar.CRUD_SERVICE.dto.request.CommentCreationRequest;
import com.mar.CRUD_SERVICE.dto.response.CommentResponse;
import com.mar.CRUD_SERVICE.model.Comment;
import com.mar.CRUD_SERVICE.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public CommentResponse createComment(CommentCreationRequest request) {
        Comment comment = new Comment();
        comment.setContent(request.getText());
        // Nếu có các trường khác, hãy set thêm ở đây
        Comment saved = commentRepository.save(comment);
        return mapToResponse(saved);
    }

    @Override
    public List<CommentResponse> getAllComments() {
        return commentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CommentResponse getCommentById(Long id) {
        return commentRepository.findById(id)
                .map(this::mapToResponse)
                .orElse(null);
    }

    @Override
    public CommentResponse updateComment(Long id, CommentCreationRequest request) {
        return commentRepository.findById(id)
                .map(comment -> {
                    comment.setContent(request.getText());
                    // Nếu có các trường khác, hãy cập nhật thêm ở đây
                    Comment updated = commentRepository.save(comment);
                    return mapToResponse(updated);
                })
                .orElse(null);
    }

    @Override
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    private CommentResponse mapToResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setText(comment.getContent());
        // Nếu có các trường khác, hãy map thêm ở đây
        return response;
    }
}
