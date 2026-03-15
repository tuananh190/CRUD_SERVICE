package com.mar.CRUD_SERVICE.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "post_hashtags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "hashtag_id")
    )
    private List<Hashtag> hashtags;

    @Column(name = "location", length = 200)
    private String location;

    @ManyToOne
    @JoinColumn(name = "shared_post_id")
    private Post sharedPost;

    @ManyToMany
    @JoinTable(
            name = "post_tagged_users",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> taggedUsers;

    @ManyToMany
    @JoinTable(
            name = "post_topics",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "topic_id")
    )
    private List<Topic> topics;

    public Post() {}

    public Post(Long id, String title, String content, LocalDateTime createdAt, List<Comment> comments, User author) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.comments = comments;
        this.author = author;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public List<Hashtag> getHashtags() { return hashtags; }
    public void setHashtags(List<Hashtag> hashtags) { this.hashtags = hashtags; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Post getSharedPost() { return sharedPost; }
    public void setSharedPost(Post sharedPost) { this.sharedPost = sharedPost; }

    public List<User> getTaggedUsers() { return taggedUsers; }
    public void setTaggedUsers(List<User> taggedUsers) { this.taggedUsers = taggedUsers; }

    public List<Topic> getTopics() { return topics; }
    public void setTopics(List<Topic> topics) { this.topics = topics; }

    // Simple builder for code compatibility
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String title;
        private String content;
        private LocalDateTime createdAt;
        private List<Comment> comments;
        private User author;
        private String location;
        private Post sharedPost;
        private List<User> taggedUsers;
        private List<Topic> topics;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder comments(List<Comment> comments) { this.comments = comments; return this; }
        public Builder author(User author) { this.author = author; return this; }
        public Builder location(String location) { this.location = location; return this; }
        public Builder sharedPost(Post sharedPost) { this.sharedPost = sharedPost; return this; }
        public Builder taggedUsers(List<User> taggedUsers) { this.taggedUsers = taggedUsers; return this; }
        public Builder topics(List<Topic> topics) { this.topics = topics; return this; }

        public Post build() { 
            Post post = new Post(id, title, content, createdAt, comments, author);
            post.setLocation(location);
            post.setSharedPost(sharedPost);
            post.setTaggedUsers(taggedUsers);
            post.setTopics(topics);
            return post;
        }
    }
}
