package com.mar.CRUD_SERVICE.dto.request;

import java.util.List;

public class CommentCreationRequest {
    private String text;
    private Long postId;
    private List<Long> taggedUserIds;

    public CommentCreationRequest() {}

    public CommentCreationRequest(String text, Long postId) {
        this.text = text;
        this.postId = postId;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public List<Long> getTaggedUserIds() { return taggedUserIds; }
    public void setTaggedUserIds(List<Long> taggedUserIds) { this.taggedUserIds = taggedUserIds; }
}