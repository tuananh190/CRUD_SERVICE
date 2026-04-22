package com.mar.CRUD_SERVICE.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToMany
    @JoinTable(
            name = "comment_tagged_users",
            joinColumns = @JoinColumn(name = "comment_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> taggedUsers;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Comment() {
        this.createdAt = LocalDateTime.now();
    }

    public Comment(Long id, String content, Post post, User author) {
        this.id = id;
        this.content = content;
        this.post = post;
        this.author = author;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public List<User> getTaggedUsers() { return taggedUsers; }
    public void setTaggedUsers(List<User> taggedUsers) { this.taggedUsers = taggedUsers; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String content;
        private Post post;
        private User author;
        private List<User> taggedUsers;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder post(Post post) { this.post = post; return this; }
        public Builder author(User author) { this.author = author; return this; }
        public Builder taggedUsers(List<User> taggedUsers) { this.taggedUsers = taggedUsers; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Comment build() { 
            Comment comment = new Comment(id, content, post, author);
            comment.setTaggedUsers(taggedUsers);
            if (createdAt != null) {
                comment.setCreatedAt(createdAt);
            }
            return comment;
        }
    }
}
