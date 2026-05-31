package com.mar.CRUD_SERVICE.dto.request;

public class ReportRequest {

    private String targetType;

    private Long targetId;

    private String reason;

    public ReportRequest() {}

    public ReportRequest(String targetType, Long targetId, String reason) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
    }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
