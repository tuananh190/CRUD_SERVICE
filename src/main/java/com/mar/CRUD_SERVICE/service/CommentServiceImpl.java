package com.mar.CRUD_SERVICE.service;

import com.mar.CRUD_SERVICE.dto.request.CommentCreationRequest;
import com.mar.CRUD_SERVICE.dto.response.CommentResponse;
import com.mar.CRUD_SERVICE.dto.response.PostResponse;
import com.mar.CRUD_SERVICE.model.Comment;
import com.mar.CRUD_SERVICE.model.Post;
import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.model.NotificationType;
import com.mar.CRUD_SERVICE.repository.CommentRepository;
import com.mar.CRUD_SERVICE.repository.PostRepository;
import com.mar.CRUD_SERVICE.repository.UserRepository;
import com.mar.CRUD_SERVICE.repository.UserInterestRepository;
import com.mar.CRUD_SERVICE.repository.ReportRepository;
import com.mar.CRUD_SERVICE.repository.NotificationRepository;
import com.mar.CRUD_SERVICE.service.NotificationService;
import com.mar.CRUD_SERVICE.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final UserInterestRepository userInterestRepository;
    private final NotificationService notificationService;
    private final ReportRepository reportRepository;
    private final NotificationRepository notificationRepository;
    private final PostService postService;
    private static final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository,
                              PostRepository postRepository,
                              UserRepository userRepository,
                              UserInterestRepository userInterestRepository,
                              NotificationService notificationService,
                              ReportRepository reportRepository,
                              NotificationRepository notificationRepository,
                              @Lazy PostService postService) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.userInterestRepository = userInterestRepository;
        this.notificationService = notificationService;
        this.reportRepository = reportRepository;
        this.notificationRepository = notificationRepository;
        this.postService = postService;
    }

    @Override
    public CommentResponse createComment(CommentCreationRequest request) {
        if (request.getText() == null || request.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung không được để trống");
        }
        log.debug("createComment called with postId={} text={}", request.getPostId(), request.getText());
        Comment comment = new Comment();
        comment.setContent(request.getText());

        if (request.getPostId() == null) {
            throw new IllegalArgumentException("postId is required");
        }
        Post post = postRepository.findById(request.getPostId()).orElseThrow(() -> new com.mar.CRUD_SERVICE.exception.ResourceNotFoundException("Post not found with id=" + request.getPostId()));
        comment.setPost(post);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new com.mar.CRUD_SERVICE.exception.AccessDeniedException("Unauthenticated: cannot determine author");
        }
        String username = auth.getName();
        if (username == null || username.isBlank()) {
            throw new com.mar.CRUD_SERVICE.exception.AccessDeniedException("Cannot determine username from authentication principal");
        }

        final String uname = username;
        log.debug("Authenticated username for comment author: {}", uname);
        User author = userRepository.findByUsername(uname).orElseThrow(() -> new com.mar.CRUD_SERVICE.exception.ResourceNotFoundException("Author user not found with username=" + uname));

        if (!postService.canUserViewPost(author, post)) {
            throw new com.mar.CRUD_SERVICE.exception.AccessDeniedException("Bạn không có quyền bình luận trên bài viết này.");
        }

        comment.setAuthor(author);

        java.util.List<String> extractedMentions = parseMentionedUsernames(request.getText());
        if (request.getText() != null) {
            comment.setContent(removeMentionSymbols(request.getText()));
        }
        if (!extractedMentions.isEmpty()) {
            comment.setTaggedUsers(userRepository.findAllByUsernameIn(extractedMentions));
        }

        Comment saved = commentRepository.save(comment);
        log.debug("Comment saved with id={}", saved.getId());

        if (post.getAuthor() != null && !post.getAuthor().getId().equals(author.getId())) {
            notificationService.createNotification(
                    post.getAuthor(),
                    author,
                    NotificationType.COMMENT,
                    post.getId()
            );
        }

        if (saved.getTaggedUsers() != null) {
            for (User tagged : saved.getTaggedUsers()) {

                if (!tagged.getId().equals(author.getId())) {
                    notificationService.createNotification(
                            tagged,
                            author,
                            NotificationType.TAG,
                            post.getId()
                    );
                }
            }
        }

        addUserInterestScore(author, post.getTopics(), 3);

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

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new com.mar.CRUD_SERVICE.exception.AccessDeniedException("Bạn chưa đăng nhập.");
        }
        String username = auth.getName();
        if (username == null || username.isBlank()) {
            throw new com.mar.CRUD_SERVICE.exception.AccessDeniedException("Không xác định được người dùng hiện tại.");
        }

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new com.mar.CRUD_SERVICE.exception.ResourceNotFoundException("Bình luận không tồn tại với id=" + id));

        if (comment.getAuthor() == null || !comment.getAuthor().getUsername().equals(username)) {
            throw new com.mar.CRUD_SERVICE.exception.AccessDeniedException("Bạn không có quyền sửa bình luận của người khác!");
        }

        comment.setContent(request.getText());
        Comment updated = commentRepository.save(comment);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteComment(Long id) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new com.mar.CRUD_SERVICE.exception.AccessDeniedException("Bạn chưa đăng nhập.");
        }
        String username = auth.getName();
        if (username == null || username.isBlank()) {
            throw new com.mar.CRUD_SERVICE.exception.AccessDeniedException("Không xác định được người dùng hiện tại.");
        }
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new com.mar.CRUD_SERVICE.exception.ResourceNotFoundException("Bình luận không tồn tại với id=" + id));

        boolean isOwner = comment.getAuthor() != null && comment.getAuthor().getUsername().equals(username);
        if (!isOwner && !isAdmin) {
            throw new com.mar.CRUD_SERVICE.exception.AccessDeniedException("Bạn không có quyền xóa bình luận của người khác!");
        }

        notificationRepository.deleteAllByReferenceId(comment.getId());
        reportRepository.deleteAllByTargetTypeAndTargetId("COMMENT", comment.getId());

        commentRepository.deleteById(id);
    }

    private CommentResponse mapToResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setText(comment.getContent());
        response.setPostId(comment.getPost() != null ? comment.getPost().getId() : null);
        response.setAuthor(toUserInfo(comment.getAuthor()));
        response.setCreatedAt(null);
        return response;
    }

    private java.util.List<String> parseMentionedUsernames(String content) {
        java.util.List<String> mentions = new java.util.ArrayList<>();
        if (content == null) return mentions;
        java.util.regex.Matcher m =
                java.util.regex.Pattern.compile("@([a-zA-Z0-9_]+)").matcher(content);
        while (m.find()) {
            mentions.add(m.group(1));
        }
        return mentions;
    }

    private String removeMentionSymbols(String content) {
        if (content == null) return null;
        return java.util.regex.Pattern.compile("@([a-zA-Z0-9_]+)")
                .matcher(content).replaceAll("$1");
    }

    private void addUserInterestScore(com.mar.CRUD_SERVICE.model.User user,
                                      java.util.List<com.mar.CRUD_SERVICE.model.Topic> topics,
                                      int weight) {
        if (topics == null || topics.isEmpty()) return;
        for (var topic : topics) {
            var interest = userInterestRepository.findByUserAndTopic(user, topic)
                    .orElse(new com.mar.CRUD_SERVICE.model.UserInterest(user, topic, 0));
            interest.setScore(interest.getScore() + weight);
            userInterestRepository.save(interest);
        }
    }

    private com.mar.CRUD_SERVICE.dto.response.PostResponse.UserInfo toUserInfo(
            com.mar.CRUD_SERVICE.model.User user) {
        if (user == null) return null;
        com.mar.CRUD_SERVICE.dto.response.PostResponse.UserInfo ui =
                new com.mar.CRUD_SERVICE.dto.response.PostResponse.UserInfo();
        ui.setId(user.getId());
        ui.setUsername(user.getUsername());
        ui.setFirstName(user.getFirstName());
        ui.setLastName(user.getLastName());
        return ui;
    }
}
