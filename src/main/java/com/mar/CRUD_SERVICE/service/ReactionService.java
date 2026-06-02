package com.mar.CRUD_SERVICE.service;

import com.mar.CRUD_SERVICE.dto.response.ReactionResponse;
import com.mar.CRUD_SERVICE.model.Comment;
import com.mar.CRUD_SERVICE.model.Post;
import com.mar.CRUD_SERVICE.model.NotificationType;
import com.mar.CRUD_SERVICE.model.Reaction;
import com.mar.CRUD_SERVICE.model.ReactionType;
import com.mar.CRUD_SERVICE.model.Topic;
import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.model.UserInterest;
import com.mar.CRUD_SERVICE.repository.CommentRepository;
import com.mar.CRUD_SERVICE.repository.PostRepository;
import com.mar.CRUD_SERVICE.repository.ReactionRepository;
import com.mar.CRUD_SERVICE.repository.UserRepository;
import com.mar.CRUD_SERVICE.service.PostService;
import com.mar.CRUD_SERVICE.service.UserInterestService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReactionService {

    private final ReactionRepository reactionRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final UserInterestService userInterestService;
    private final NotificationService notificationService;
    private final PostService postService;

    public ReactionService(ReactionRepository reactionRepository,
            PostRepository postRepository,
            CommentRepository commentRepository,
            UserRepository userRepository,
            UserInterestService userInterestService,
            NotificationService notificationService,
            @Lazy PostService postService) {
        this.reactionRepository = reactionRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.userInterestService = userInterestService;
        this.notificationService = notificationService;
        this.postService = postService;
    }

    @Transactional
    public ReactionResponse reactToPost(Long postId, String username, ReactionType type) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + username));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết ID: " + postId));

        if (!postService.canUserViewPost(user, post)) {
            throw new IllegalStateException("Bạn không có quyền tương tác với bài viết này.");
        }

        Reaction existing = reactionRepository.findByUserAndPost(user, post).orElse(null);

        String action;

        if (existing == null) {

            Reaction newReaction = new Reaction(user, post, null, type, LocalDateTime.now());
            reactionRepository.save(newReaction);
            action = "ADDED";

            if (post.getAuthor() != null && !post.getAuthor().getId().equals(user.getId())) {
                notificationService.createNotification(post.getAuthor(), user, NotificationType.LIKE, postId);
            }

            if (post.getTopics() != null) {
                int weight = (type == ReactionType.LIKE) ? 1 : -2;
                userInterestService.addUserInterestScore(user, post.getTopics(), weight);
            }

        } else if (existing.getType() == type) {

            reactionRepository.delete(existing);
            action = "REMOVED";

        } else {

            ReactionType oldType = existing.getType();
            existing.setType(type);
            existing.setCreatedAt(LocalDateTime.now());
            reactionRepository.save(existing);
            action = "CHANGED";

            if (post.getTopics() != null) {
                int weightDiff = 0;
                if (oldType == ReactionType.LIKE && type == ReactionType.ANGRY) {
                    weightDiff = -3;
                } else if (oldType == ReactionType.ANGRY && type == ReactionType.LIKE) {
                    weightDiff = 3;
                }

                if (weightDiff != 0) {
                    userInterestService.addUserInterestScore(user, post.getTopics(), weightDiff);
                }
            }

            if (post.getAuthor() != null && !post.getAuthor().getId().equals(user.getId())) {
                notificationService.createNotification(post.getAuthor(), user, NotificationType.LIKE, postId);
            }
        }

        return buildPostReactionResponse(action, action.equals("REMOVED") ? null : type, postId);
    }

    @Transactional
    public ReactionResponse reactToComment(Long commentId, String username, ReactionType type) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + username));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy comment ID: " + commentId));

        Reaction existing = reactionRepository.findByUserAndComment(user, comment).orElse(null);
        String action;

        if (existing == null) {

            Reaction newReaction = new Reaction(user, null, comment, type, LocalDateTime.now());
            reactionRepository.save(newReaction);
            action = "ADDED";

            if (comment.getAuthor() != null && !comment.getAuthor().getId().equals(user.getId())) {
                notificationService.createNotification(comment.getAuthor(), user, NotificationType.LIKE, commentId);
            }

            if (comment.getPost() != null && comment.getPost().getTopics() != null) {
                int weight = (type == ReactionType.LIKE) ? 1 : -2;
                userInterestService.addUserInterestScore(user, comment.getPost().getTopics(), weight);
            }

        } else if (existing.getType() == type) {

            reactionRepository.delete(existing);
            action = "REMOVED";

        } else {

            ReactionType oldType = existing.getType();
            existing.setType(type);
            existing.setCreatedAt(LocalDateTime.now());
            reactionRepository.save(existing);
            action = "CHANGED";

            if (comment.getPost() != null && comment.getPost().getTopics() != null) {
                int weightDiff = 0;
                if (oldType == ReactionType.LIKE && type == ReactionType.ANGRY) {
                    weightDiff = -3;
                } else if (oldType == ReactionType.ANGRY && type == ReactionType.LIKE) {
                    weightDiff = 3;
                }

                if (weightDiff != 0) {
                    userInterestService.addUserInterestScore(user, comment.getPost().getTopics(), weightDiff);
                }
            }

            if (comment.getAuthor() != null && !comment.getAuthor().getId().equals(user.getId())) {
                notificationService.createNotification(comment.getAuthor(), user, NotificationType.LIKE, commentId);
            }
        }

        return buildCommentReactionResponse(action, action.equals("REMOVED") ? null : type, commentId);
    }

    private ReactionResponse buildPostReactionResponse(String action, ReactionType type, Long postId) {
        long total = reactionRepository.countByPostId(postId);
        Map<String, Long> breakdown = buildBreakdown(postId, true);
        return new ReactionResponse(
                action,
                type != null ? type.name() : null,
                total,
                breakdown);
    }

    private ReactionResponse buildCommentReactionResponse(String action, ReactionType type, Long commentId) {
        long total = reactionRepository.countByCommentId(commentId);
        Map<String, Long> breakdown = buildCommentBreakdown(commentId);
        return new ReactionResponse(
                action,
                type != null ? type.name() : null,
                total,
                breakdown);
    }

    private Map<String, Long> buildBreakdown(Long postId, boolean isPost) {
        Map<String, Long> breakdown = new HashMap<>();
        for (ReactionType t : ReactionType.values()) {
            long count = reactionRepository.countByPostIdAndType(postId, t);
            if (count > 0) {
                breakdown.put(t.name(), count);
            }
        }
        return breakdown;
    }

    private Map<String, Long> buildCommentBreakdown(Long commentId) {
        Map<String, Long> breakdown = new HashMap<>();
        for (ReactionType t : ReactionType.values()) {
            long count = reactionRepository.countByCommentIdAndType(commentId, t);
            if (count > 0) {
                breakdown.put(t.name(), count);
            }
        }
        return breakdown;
    }

}
