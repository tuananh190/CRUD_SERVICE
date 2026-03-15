package com.mar.CRUD_SERVICE.repository;

import com.mar.CRUD_SERVICE.model.Friendship;
import com.mar.CRUD_SERVICE.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    
    @Query("SELECT f FROM Friendship f WHERE (f.user1 = :u1 AND f.user2 = :u2) OR (f.user1 = :u2 AND f.user2 = :u1)")
    Optional<Friendship> findByUsers(User u1, User u2);
    
    List<Friendship> findByUser2AndStatus(User user2, String status); // to find pending requests for user2
    
    @Query("SELECT f FROM Friendship f WHERE (f.user1 = :user OR f.user2 = :user) AND f.status = 'ACCEPTED'")
    List<Friendship> findAcceptedFriendshipsByUser(User user);
}
