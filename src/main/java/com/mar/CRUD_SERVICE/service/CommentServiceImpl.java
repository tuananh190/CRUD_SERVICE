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
import com.mar.CRUD_SERVICE.repository.UserInterestRepository;
import com.mar.CRUD_SERVICE.service.NotificationService;
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
    private final UserInterestRepository userInterestRepository;
    private final NotificationService notificationService;
    private static final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository,
                              PostRepository postRepository,
                              UserRepository userRepository,
                              UserInterestRepository userInterestRepository,
                              NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.userInterestRepository = userInterestRepository;
        this.notificationService = notificationService;
    }

    @Override
    public CommentResponse createComment(CommentCreationRequest request) {
        log.debug("createComment called with postId={} text={}", request.getPostId(), request.getText());
        Comment comment = new Comment();
        comment.setContent(request.getText());


        if (request.getPostId() == null) {
            throw new IllegalStateException("postId is required");
        }
        Post post = postRepository.findById(request.getPostId()).orElseThrow(() -> new IllegalStateException("Post not found with id=" + request.getPostId()));
        comment.setPost(post);

        // lấy author từ SecurityContext
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

        final String uname = username;
        log.debug("Authenticated username for comment author: {}", uname);
        User author = userRepository.findByUsername(uname).orElseThrow(() -> new IllegalStateException("Author user not found with username=" + uname));
        comment.setAuthor(author);

        // map tagged users nếu có
        if (request.getTaggedUserIds() != null && !request.getTaggedUserIds().isEmpty()) {
            var taggedUsers = userRepository.findAllById(request.getTaggedUserIds());
            comment.setTaggedUsers(taggedUsers);
        }

        Comment saved = commentRepository.save(comment);
        log.debug("Comment saved with id={}", saved.getId());

        // gửi thông báo cho author của post khi có người khác comment
        if (post.getAuthor() != null && !post.getAuthor().getId().equals(author.getId())) {
            notificationService.createNotification(
                    post.getAuthor(),
                    "Bài viết của bạn vừa nhận được một bình luận mới từ @" + author.getUsername()
            );
        }

        // gửi thông báo cho các user được tag trong comment
        if (saved.getTaggedUsers() != null) {
            for (User tagged : saved.getTaggedUsers()) {
                // tránh gửi thông báo cho chính người đang comment
                if (!tagged.getId().equals(author.getId())) {
                    notificationService.createNotification(
                            tagged,
                            "Bạn được nhắc đến trong một bình luận của @" + author.getUsername()
                    );
                }
            }
        }

        // Cập nhật UserInterest khi người dùng comment (Comment -> +3)
        if (post.getTopics() != null && !post.getTopics().isEmpty()) {
            for (var topic : post.getTopics()) {
                var interest = userInterestRepository.findByUserAndTopic(author, topic)
                        .orElse(new com.mar.CRUD_SERVICE.model.UserInterest(author, topic, 0));
                interest.setScore(interest.getScore() + 3); // Comment -> +3
                userInterestRepository.save(interest);
            }
        }

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
