package com.mar.CRUD_SERVICE.dto.response;

import java.time.LocalDateTime;

public class PostListResponse {
    private Long id;
    private String content;
    private Long userId;
    private LocalDateTime createdAt;
    private long reactionCount;
    private int commentCount;

    public PostListResponse() {}

    public PostListResponse(Long id, String content, Long userId, LocalDateTime createdAt, long reactionCount, int commentCount) {
        this.id = id;
        this.content = content;
        this.userId = userId;
        this.createdAt = createdAt;
        this.reactionCount = reactionCount;
        this.commentCount = commentCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public long getReactionCount() { return reactionCount; }
    public void setReactionCount(long reactionCount) { this.reactionCount = reactionCount; }

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }
}
