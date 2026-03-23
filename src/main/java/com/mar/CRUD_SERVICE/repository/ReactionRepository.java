package com.mar.CRUD_SERVICE.repository;

import com.mar.CRUD_SERVICE.model.Reaction;
import com.mar.CRUD_SERVICE.model.Post;
import com.mar.CRUD_SERVICE.model.Comment;
import com.mar.CRUD_SERVICE.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    Optional<Reaction> findByUserAndPost(User user, Post post);
    Optional<Reaction> findByUserAndComment(User user, Comment comment);
    long countByPostId(Long postId);
    long countByCommentId(Long commentId);
    long countByPostIdAndType(Long postId, com.mar.CRUD_SERVICE.model.ReactionType type);
}
