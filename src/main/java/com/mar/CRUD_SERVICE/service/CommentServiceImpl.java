package com.mar.CRUD_SERVICE.service;

import com.mar.CRUD_SERVICE.dto.request.CommentCreationRequest;
import com.mar.CRUD_SERVICE.dto.response.CommentResponse;
import com.mar.CRUD_SERVICE.dto.response.PostResponse;
import com.mar.CRUD_SERVICE.model.Comment;
import com.mar.CRUD_SERVICE.model.Post;
import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.repository.CommentRepository;
import com.mar.CRUD_SERVICE.repository.PostRepository;
import com.mar.CRUD_SERVICE.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CommentResponse createComment(CommentCreationRequest request) {
        log.debug("createComment called with postId={} text={}", request.getPostId(), request.getText());
        Comment comment = new Comment();
        comment.setContent(request.getText());

        // Lấy post
        if (request.getPostId() == null) {
            throw new IllegalStateException("postId is required");
        }
        Post post = postRepository.findById(request.getPostId()).orElseThrow(() -> new IllegalStateException("Post not found with id=" + request.getPostId()));
        comment.setPost(post);

        // Lấy author từ SecurityContext an toàn
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = null;
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("Unauthenticated: cannot determine author");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        }
        if (username == null) {
            throw new IllegalStateException("Cannot determine username from authentication principal");
        }
        // make a final copy so it can be safely referenced from lambdas
        final String uname = username;
        log.debug("Authenticated username for comment author: {}", uname);
        User author = userRepository.findByUsername(uname).orElseThrow(() -> new IllegalStateException("Author user not found with username=" + uname));
        comment.setAuthor(author);

        Comment saved = commentRepository.save(comment);
        log.debug("Comment saved with id={}", saved.getId());
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
                    // Không thay post/author trong update
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
        response.setPostId(comment.getPost() != null ? comment.getPost().getId() : null);
        if (comment.getAuthor() != null) {
            PostResponse.UserInfo authorInfo = new PostResponse.UserInfo();
            authorInfo.setId(comment.getAuthor().getId());
            authorInfo.setUsername(comment.getAuthor().getUsername());
            authorInfo.setFirstName(comment.getAuthor().getFirstName());
            authorInfo.setLastName(comment.getAuthor().getLastName());
            response.setAuthor(authorInfo);
        }
        response.setCreatedAt(null); // map if you have createdAt
        return response;
    }
}
