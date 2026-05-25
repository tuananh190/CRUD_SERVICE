package com.mar.CRUD_SERVICE.dto.request;

/**
 * Request body cho API gửi báo cáo vi phạm.
 *
 * Ví dụ JSON khi người dùng báo cáo bài viết:
 * {
 *   "targetType": "POST",
 *   "targetId": 42,
 *   "reason": "Bài viết này chứa nội dung bạo lực không phù hợp"
 * }
 */
public class ReportRequest {

    /**
     * Loại nội dung bị báo cáo.
     * Chấp nhận: "POST" hoặc "COMMENT"
     * Validation được thực hiện ở Service layer (không dùng @Valid để giữ đơn giản).
     */
    private String targetType;

    /** ID của bài viết hoặc comment bị báo cáo. */
    private Long targetId;

    /**
     * Lý do báo cáo.
     * Bắt buộc, không được để trống — validation tại Service.
     */
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
