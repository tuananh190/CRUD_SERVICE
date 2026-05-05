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
import com.mar.CRUD_SERVICE.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
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

        // Lấy username của người đang đăng nhập từ SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("Unauthenticated: cannot determine author");
        }
        String username = auth.getName();
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("Cannot determine username from authentication principal");
        }

        final String uname = username;
        log.debug("Authenticated username for comment author: {}", uname);
        User author = userRepository.findByUsername(uname).orElseThrow(() -> new IllegalStateException("Author user not found with username=" + uname));
        comment.setAuthor(author);

        // map tagged users từ content (dùng helper method)
        java.util.List<String> extractedMentions = parseMentionedUsernames(request.getText());
        if (request.getText() != null) {
            comment.setContent(removeMentionSymbols(request.getText()));
        }
        if (!extractedMentions.isEmpty()) {
            comment.setTaggedUsers(userRepository.findAllByUsernameIn(extractedMentions));
        }

        Comment saved = commentRepository.save(comment);
        log.debug("Comment saved with id={}", saved.getId());

        // gửi thông báo cho author của post khi có người khác comment
        if (post.getAuthor() != null && !post.getAuthor().getId().equals(author.getId())) {
            notificationService.createNotification(
                    post.getAuthor(),
                    author,
                    NotificationType.COMMENT,
                    post.getId()
            );
        }

        // gửi thông báo cho các user được tag trong comment
        if (saved.getTaggedUsers() != null) {
            for (User tagged : saved.getTaggedUsers()) {
                // tránh gửi thông báo cho chính người đang comment
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

        // Cập nhật UserInterest khi người dùng comment (Comment -> +3)
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
        // Xác định người dùng đang đăng nhập
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("Bạn chưa đăng nhập.");
        }
        String username = auth.getName();
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("Không xác định được người dùng hiện tại.");
        }

        // Tìm comment cần xóa
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Bình luận không tồn tại với id=" + id));

        // Chỉ chủ nhân mới được xóa bình luận của mình
        if (comment.getAuthor() == null || !comment.getAuthor().getUsername().equals(username)) {
            throw new IllegalStateException("Bạn không có quyền xóa bình luận của người khác!");
        }

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

    // ================================================
    // Private Helper Methods
    // ================================================

    /** Trích xuất danh sách username được @mention trong nội dung. */
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

    /** Loại bỏ ký tự @ khỏi nội dung: "@username" → "username". */
    private String removeMentionSymbols(String content) {
        if (content == null) return null;
        return java.util.regex.Pattern.compile("@([a-zA-Z0-9_]+)")
                .matcher(content).replaceAll("$1");
    }

    /** Cập nhật điểm UserInterest cho user theo từng topic. */
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

    /** Chuyển đổi User entity sang UserInfo DTO. */
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
