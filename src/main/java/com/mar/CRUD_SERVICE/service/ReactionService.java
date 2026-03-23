package com.mar.CRUD_SERVICE.service;

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
import com.mar.CRUD_SERVICE.repository.UserInterestRepository;
import com.mar.CRUD_SERVICE.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReactionService {

    private final ReactionRepository reactionRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final UserInterestRepository userInterestRepository;
    private final NotificationService notificationService;

    public ReactionService(ReactionRepository reactionRepository,
                           PostRepository postRepository,
                           CommentRepository commentRepository,
                           UserRepository userRepository,
                           UserInterestRepository userInterestRepository,
                           NotificationService notificationService) {
        this.reactionRepository = reactionRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.userInterestRepository = userInterestRepository;
        this.notificationService = notificationService;
    }

    public String reactToPost(Long postId, String username, ReactionType type) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + username));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết ID: " + postId));

        Reaction reaction = reactionRepository.findByUserAndPost(user, post).orElse(null);
        if (reaction != null) {
            // nếu cùng loại thì coi như bỏ reaction
            if (reaction.getType() == type) {
                reactionRepository.delete(reaction);
                return buildPostCountMessage(postId, "Đã huỷ reaction trên bài viết. Tổng: ");
            }
            // đổi loại reaction
            reaction.setType(type);
            reactionRepository.save(reaction);
        } else {
            reaction = new Reaction(user, post, null, type, LocalDateTime.now());
            reactionRepository.save(reaction);
        }

        // notification cho chủ bài viết (tránh tự gửi cho chính mình)
        if (post.getAuthor() != null && !post.getAuthor().getId().equals(user.getId())) {
            notificationService.createNotification(post.getAuthor(), user, NotificationType.LIKE, postId);
        }

        // cập nhật interest score chỉ cho LIKE (theo đặc tả Like=+1)
        if (type == ReactionType.LIKE && post.getTopics() != null) {
            updateUserInterest(user, post.getTopics(), 1);
        }

        return buildPostCountMessage(postId, "Reaction trên bài viết đã được cập nhật. Tổng: ");
    }

    public String reactToComment(Long commentId, String username, ReactionType type) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + username));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy comment ID: " + commentId));

        Reaction reaction = reactionRepository.findByUserAndComment(user, comment).orElse(null);
        if (reaction != null) {
            if (reaction.getType() == type) {
                reactionRepository.delete(reaction);
                return buildCommentCountMessage(commentId, "Đã huỷ reaction trên comment. Tổng: ");
            }
            reaction.setType(type);
            reactionRepository.save(reaction);
        } else {
            reaction = new Reaction(user, null, comment, type, LocalDateTime.now());
            reactionRepository.save(reaction);
        }

        // notification cho tác giả comment (tránh tự gửi cho chính mình)
        if (comment.getAuthor() != null && !comment.getAuthor().getId().equals(user.getId())) {
            notificationService.createNotification(comment.getAuthor(), user, NotificationType.LIKE, commentId);
        }

        // interest cho LIKE dựa trên topics của bài viết chứa comment (nếu có)
        if (type == ReactionType.LIKE && comment.getPost() != null && comment.getPost().getTopics() != null) {
            updateUserInterest(user, comment.getPost().getTopics(), 1);
        }

        return buildCommentCountMessage(commentId, "Reaction trên comment đã được cập nhật. Tổng: ");
    }

    private void updateUserInterest(User user, List<Topic> topics, int weight) {
        for (Topic topic : topics) {
            UserInterest interest = userInterestRepository.findByUserAndTopic(user, topic)
                    .orElse(new UserInterest(user, topic, 0));
            interest.setScore(interest.getScore() + weight);
            userInterestRepository.save(interest);
        }
    }

    private String buildPostCountMessage(Long postId, String prefix) {
        long count = reactionRepository.countByPostId(postId);
        return prefix + count;
    }

    private String buildCommentCountMessage(Long commentId, String prefix) {
        long count = reactionRepository.countByCommentId(commentId);
        return prefix + count;
    }
}

