package com.mar.CRUD_SERVICE.repository;

import com.mar.CRUD_SERVICE.model.Post;
import com.mar.CRUD_SERVICE.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByAuthor(User author);
    List<Post> findByAuthorId(Long authorId);
}