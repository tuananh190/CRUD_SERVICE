package com.mar.CRUD_SERVICE.dto.request;

import java.util.List;

public class PostCreationRequest {
    private String title;
    private String content;
    private Long userId;

    private String location;
    private Long sharedPostId;
    private List<Long> taggedUserIds;
    private List<String> hashtags;

    public PostCreationRequest() {}

    public PostCreationRequest(String title, String content, Long userId) {
        this.title = title;
        this.content = content;
        this.userId = userId;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Long getSharedPostId() { return sharedPostId; }
    public void setSharedPostId(Long sharedPostId) { this.sharedPostId = sharedPostId; }

    public List<Long> getTaggedUserIds() { return taggedUserIds; }
    public void setTaggedUserIds(List<Long> taggedUserIds) { this.taggedUserIds = taggedUserIds; }

    public List<String> getHashtags() { return hashtags; }
    public void setHashtags(List<String> hashtags) { this.hashtags = hashtags; }
}