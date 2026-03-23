package com.mar.CRUD_SERVICE.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Friendship model representing friend requests between users.
 *
 * Status States (BR-21 - Business Rule 21):
 * - PENDING: Friend request sent, awaiting response from user2
 * - ACCEPTED: Friend request accepted, users are now friends
 * - REJECTED: Friend request declined by user2 (can be re-sent later)
 *
 * Flow:
 * 1. sendFriendRequest() → Creates PENDING friendship (user1 is sender, user2 is receiver)
 * 2. acceptFriendRequest() → Changes PENDING → ACCEPTED
 * 3. declineFriendRequest() → Changes PENDING → REJECTED (history is kept)
 * 4. unfriend() → Deletes ACCEPTED friendship (friendship ends)
 * 5. sendFriendRequest() after REJECTED → Re-sends request by setting REJECTED → PENDING
 */
@Entity
@Table(name = "friendships", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user1_id", "user2_id"})
})
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;

    @ManyToOne
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2;

    @Column(nullable = false)
    private String status; // PENDING, ACCEPTED

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Friendship() {}

    public Friendship(User user1, User user2, String status, LocalDateTime createdAt) {
        this.user1 = user1;
        this.user2 = user2;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser1() { return user1; }
    public void setUser1(User user1) { this.user1 = user1; }

    public User getUser2() { return user2; }
    public void setUser2(User user2) { this.user2 = user2; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
