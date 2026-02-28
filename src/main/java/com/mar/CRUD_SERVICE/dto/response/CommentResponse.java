package com.mar.CRUD_SERVICE.dto.response;

import com.mar.CRUD_SERVICE.dto.response.PostResponse.UserInfo; // reuse UserInfo

import java.time.LocalDateTime;

public class CommentResponse {
    private Long id;
    private String text;
    private LocalDateTime createdAt;
    private UserInfo author;
    private Long postId;

    public CommentResponse() {}

    public CommentResponse(Long id, String text, LocalDateTime createdAt, UserInfo author, Long postId) {
        this.id = id;
        this.text = text;
        this.createdAt = createdAt;
        this.author = author;
        this.postId = postId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public UserInfo getAuthor() { return author; }
    public void setAuthor(UserInfo author) { this.author = author; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
}