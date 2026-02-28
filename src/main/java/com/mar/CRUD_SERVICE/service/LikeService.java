package com.mar.CRUD_SERVICE.service;

import com.mar.CRUD_SERVICE.model.Comment;
import com.mar.CRUD_SERVICE.model.Like;
import com.mar.CRUD_SERVICE.model.Post;
import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.repository.CommentRepository;
import com.mar.CRUD_SERVICE.repository.LikeRepository;
import com.mar.CRUD_SERVICE.repository.PostRepository;
import com.mar.CRUD_SERVICE.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public LikeService(LikeRepository likeRepository, PostRepository postRepository,
                       CommentRepository commentRepository, UserRepository userRepository) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    public String likePost(Long postId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + username));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết ID: " + postId));

        if (likeRepository.existsByUserAndPost(user, post)) {
            throw new IllegalStateException("Bạn đã like bài viết này rồi.");
        }

        Like like = new Like(user, post, null, LocalDateTime.now());
        likeRepository.save(like);
        return "Đã like bài viết thành công! Tổng: " + likeRepository.countByPost(post);
    }

    public String unlikePost(Long postId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + username));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết ID: " + postId));

        Like like = likeRepository.findByUserAndPost(user, post)
                .orElseThrow(() -> new IllegalStateException("Bạn chưa like bài viết này."));
        likeRepository.delete(like);
        return "Đã unlike bài viết. Tổng: " + likeRepository.countByPost(post);
    }

    public String likeComment(Long commentId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + username));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy comment ID: " + commentId));

        if (likeRepository.existsByUserAndComment(user, comment)) {
            throw new IllegalStateException("Bạn đã like comment này rồi.");
        }

        Like like = new Like(user, null, comment, LocalDateTime.now());
        likeRepository.save(like);
        return "Đã like comment thành công! Tổng: " + likeRepository.countByComment(comment);
    }

    public String unlikeComment(Long commentId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + username));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy comment ID: " + commentId));

        Like like = likeRepository.findByUserAndComment(user, comment)
                .orElseThrow(() -> new IllegalStateException("Bạn chưa like comment này."));
        likeRepository.delete(like);
        return "Đã unlike comment. Tổng: " + likeRepository.countByComment(comment);
    }
}
