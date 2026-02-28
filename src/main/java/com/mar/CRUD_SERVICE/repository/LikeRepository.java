package com.mar.CRUD_SERVICE.repository;

import com.mar.CRUD_SERVICE.model.Like;
import com.mar.CRUD_SERVICE.model.Post;
import com.mar.CRUD_SERVICE.model.Comment;
import com.mar.CRUD_SERVICE.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserAndPost(User user, Post post);
    Optional<Like> findByUserAndComment(User user, Comment comment);
    long countByPost(Post post);
    long countByComment(Comment comment);
    boolean existsByUserAndPost(User user, Post post);
    boolean existsByUserAndComment(User user, Comment comment);
}
