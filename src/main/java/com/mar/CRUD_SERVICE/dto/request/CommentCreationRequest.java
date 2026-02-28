package com.mar.CRUD_SERVICE.dto.request;

public class CommentCreationRequest {
    private String text;
    private Long postId;

    public CommentCreationRequest() {}

    public CommentCreationRequest(String text, Long postId) {
        this.text = text;
        this.postId = postId;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
}