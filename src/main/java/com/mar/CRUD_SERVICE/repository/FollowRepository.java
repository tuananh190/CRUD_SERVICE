package com.mar.CRUD_SERVICE.repository;

import com.mar.CRUD_SERVICE.model.Follow;
import com.mar.CRUD_SERVICE.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
    boolean existsByFollowerAndFollowing(User follower, User following);
    List<Follow> findByFollowing(User following);   // danh sách followers
    List<Follow> findByFollower(User follower);      // danh sách đang following
}
