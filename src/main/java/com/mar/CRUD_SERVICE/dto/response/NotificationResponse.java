package com.mar.CRUD_SERVICE.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class NotificationResponse {

    private Long id;

    @JsonProperty("type")
    private String type;

    @JsonProperty("sender_id")
    private Long senderId;

    @JsonProperty("sender_username")
    private String senderUsername;

    @JsonProperty("reference_id")
    private Long referenceId;

    @JsonProperty("is_read")
    private boolean read;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("message")
    private String message;

    public NotificationResponse() {}

    public NotificationResponse(Long id, String type, Long senderId, String senderUsername,
                                Long referenceId, boolean read, LocalDateTime createdAt, String message) {
        this.id = id;
        this.type = type;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.referenceId = referenceId;
        this.read = read;
        this.createdAt = createdAt;
        this.message = message;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
