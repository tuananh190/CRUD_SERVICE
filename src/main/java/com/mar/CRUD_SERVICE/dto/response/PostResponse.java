package com.mar.CRUD_SERVICE.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class PostResponse {

    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;

    private UserInfo author;
    private List<CommentResponse> comments;

    public PostResponse() {}

    public PostResponse(Long id, String title, String content, LocalDateTime createdAt, UserInfo author, List<CommentResponse> comments) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.author = author;
        this.comments = comments;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public UserInfo getAuthor() { return author; }
    public void setAuthor(UserInfo author) { this.author = author; }

    public java.util.List<CommentResponse> getComments() { return comments; }
    public void setComments(java.util.List<CommentResponse> comments) { this.comments = comments; }

    public static class UserInfo {
        private Long id;
        private String username;
        private String firstName;
        private String lastName;

        public UserInfo() {}

        public UserInfo(Long id, String username, String firstName, String lastName) {
            this.id = id;
            this.username = username;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
    }
}