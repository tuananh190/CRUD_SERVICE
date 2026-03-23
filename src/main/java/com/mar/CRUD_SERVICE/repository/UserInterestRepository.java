package com.mar.CRUD_SERVICE.repository;

import com.mar.CRUD_SERVICE.model.UserInterest;
import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {
    Optional<UserInterest> findByUserAndTopic(User user, Topic topic);
    List<UserInterest> findByUserOrderByScoreDesc(User user);
}
