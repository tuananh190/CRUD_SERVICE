package com.mar.CRUD_SERVICE.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "hidden_posts",
    uniqueConstraints = {

        @UniqueConstraint(columnNames = {"user_id", "post_id"})
    },
    indexes = {

        @Index(name = "idx_hidden_user", columnList = "user_id")
    }
)
public class HiddenPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "hidden_at", nullable = false)
    private LocalDateTime hiddenAt;

    public HiddenPost() {}

    public HiddenPost(User user, Post post) {
        this.user = user;
        this.post = post;
        this.hiddenAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }

    public LocalDateTime getHiddenAt() { return hiddenAt; }
    public void setHiddenAt(LocalDateTime hiddenAt) { this.hiddenAt = hiddenAt; }
}
