package com.mar.CRUD_SERVICE.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_interests", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "topic_id"})
})
public class UserInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(nullable = false)
    private int score = 0;

    public UserInterest() {}

    public UserInterest(User user, Topic topic, int score) {
        this.user = user;
        this.topic = topic;
        this.score = score;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Topic getTopic() { return topic; }
    public void setTopic(Topic topic) { this.topic = topic; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}
