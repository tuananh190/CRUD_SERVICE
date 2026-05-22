package com.mar.CRUD_SERVICE.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mar.CRUD_SERVICE.model.Visibility;

import java.util.List;

public class PostCreationRequest {
    private String title;
    private String content;
    private Long userId;

    @JsonProperty("location_name")
    private String locationName;

    private List<Long> taggedUserIds;
    private List<String> hashtags;

    // Tuỳ chọn: PUBLIC (mặc định) hoặc FRIENDS_ONLY
    // Nếu không truyền → hệ thống tự đặt PUBLIC
    private Visibility visibility;

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

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }



    public List<Long> getTaggedUserIds() { return taggedUserIds; }
    public void setTaggedUserIds(List<Long> taggedUserIds) { this.taggedUserIds = taggedUserIds; }

    public List<String> getHashtags() { return hashtags; }
    public void setHashtags(List<String> hashtags) { this.hashtags = hashtags; }

    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }
}