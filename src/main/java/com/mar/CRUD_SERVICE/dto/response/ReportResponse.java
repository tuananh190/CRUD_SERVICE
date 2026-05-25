package com.mar.CRUD_SERVICE.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

/**
 * DTO trả về thông tin một Report cho Admin xem xét.
 *
 * Che giấu thông tin nội bộ của entity (FetchType.LAZY references)
 * và chỉ expose những gì Admin cần thấy.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportResponse {

    private Long id;

    /** Username của người đã báo cáo (không expose toàn bộ User object) */
    private String reporterUsername;

    /** Loại nội dung: "POST" hoặc "COMMENT" */
    private String targetType;

    /** ID của nội dung bị báo cáo */
    private Long targetId;

    /** Lý do báo cáo */
    private String reason;

    /** Trạng thái xử lý: PENDING / RESOLVED / DISMISSED */
    private String status;

    /** Ghi chú của Admin (nếu có) */
    private String adminNote;

    private LocalDateTime createdAt;

    /** Thời điểm Admin xử lý — null nếu chưa xử lý */
    private LocalDateTime resolvedAt;

    public ReportResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReporterUsername() { return reporterUsername; }
    public void setReporterUsername(String reporterUsername) { this.reporterUsername = reporterUsername; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
