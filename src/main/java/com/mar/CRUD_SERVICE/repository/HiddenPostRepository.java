package com.mar.CRUD_SERVICE.repository;

import com.mar.CRUD_SERVICE.model.HiddenPost;
import com.mar.CRUD_SERVICE.model.Post;
import com.mar.CRUD_SERVICE.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface HiddenPostRepository extends JpaRepository<HiddenPost, Long> {

    boolean existsByUserAndPost(User user, Post post);

    Optional<HiddenPost> findByUserAndPost(User user, Post post);

    @Query("SELECT h.post.id FROM HiddenPost h WHERE h.user = :user")
    Set<Long> findHiddenPostIdsByUser(@Param("user") User user);
}
