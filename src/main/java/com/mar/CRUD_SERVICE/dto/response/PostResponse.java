package com.mar.CRUD_SERVICE.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class PostResponse {

    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    @JsonProperty("location_name")
    private String locationName;
    private Double latitude;
    private Double longitude;

    private UserInfo author;
    private List<CommentResponse> comments;
    

    private List<UserInfo> taggedUsers;
    private List<String> topics;
    
    // new fields for interaction representation
    private Map<String, Long> reactionCounts;
    private String currentUserReaction;

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

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }



    public List<UserInfo> getTaggedUsers() { return taggedUsers; }
    public void setTaggedUsers(List<UserInfo> taggedUsers) { this.taggedUsers = taggedUsers; }

    public List<String> getTopics() { return topics; }
    public void setTopics(List<String> topics) { this.topics = topics; }

    public Map<String, Long> getReactionCounts() { return reactionCounts; }
    public void setReactionCounts(Map<String, Long> reactionCounts) { this.reactionCounts = reactionCounts; }

    public String getCurrentUserReaction() { return currentUserReaction; }
    public void setCurrentUserReaction(String currentUserReaction) { this.currentUserReaction = currentUserReaction; }

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