package com.mar.CRUD_SERVICE.model;
import jakarta.persistence.*;

@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    public Comment() {}

    public Comment(Long id, String content, Post post, User author) {
        this.id = id;
        this.content = content;
        this.post = post;
        this.author = author;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String content;
        private Post post;
        private User author;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder post(Post post) { this.post = post; return this; }
        public Builder author(User author) { this.author = author; return this; }

        public Comment build() { return new Comment(id, content, post, author); }
    }
}
