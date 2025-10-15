package com.mar.CRUD_SERVICE.repository;

import com.mar.CRUD_SERVICE.model.Comment;
import com.mar.CRUD_SERVICE.model.Post;
import com.mar.CRUD_SERVICE.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPost(Post post);
    List<Comment> findByAuthor(User author);
    List<Comment> findByAuthorId(Long authorId);
}