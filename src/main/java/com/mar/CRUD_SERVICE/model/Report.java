package com.mar.CRUD_SERVICE.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity lưu trữ báo cáo vi phạm nội dung (Content Report).
 *
 * =====================================================================
 * THIẾT KẾ "PLUG-AND-PLAY" — ĐỘC LẬP HOÀN TOÀN
 * =====================================================================
 * Bảng "reports" KHÔNG có foreign key ràng buộc cứng sang bảng "posts"
 * hay "comments". Thay vào đó, target được lưu theo dạng (targetType, targetId):
 *
 *   targetType = "POST"    + targetId = 5  → Bài viết ID 5
 *   targetType = "COMMENT" + targetId = 12 → Comment ID 12
 *
 * Kỹ thuật này gọi là "Polymorphic Association" — cho phép module Report
 * mở rộng để report thêm các loại nội dung mới (USER, STORY...) trong
 * tương lai mà KHÔNG cần thêm cột vào entity tương ứng.
 *
 * Vòng đời của Report (ReportStatus):
 *   PENDING   → Admin chưa xử lý (trạng thái ban đầu)
 *   RESOLVED  → Admin xác nhận vi phạm → xóa nội dung
 *   DISMISSED → Admin từ chối → nội dung không vi phạm
 * =====================================================================
 *
 * DB Impact: Chỉ tạo thêm bảng "reports" mới.
 * Bảng "posts", "comments", "users" KHÔNG bị chỉnh sửa.
 */
@Entity
@Table(
    name = "reports",
    indexes = {
        // Index hỗ trợ query "Lấy danh sách report theo trạng thái" (Admin dashboard)
        @Index(name = "idx_report_status", columnList = "status"),
        // Index hỗ trợ query "User này đã report bài này chưa?" (chống spam report)
        @Index(name = "idx_report_reporter", columnList = "reporter_id")
    }
)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Người gửi báo cáo.
     * FetchType.LAZY để tránh join không cần thiết khi Admin chỉ cần xem targetType/targetId.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    /**
     * Loại nội dung bị báo cáo: "POST" hoặc "COMMENT".
     * Dùng String thay vì Enum để dễ mở rộng (ví dụ: "USER", "STORY") mà không cần migrate.
     */
    @Column(name = "target_type", nullable = false, length = 20)
    private String targetType; // "POST" | "COMMENT"

    /**
     * ID của nội dung bị báo cáo (postId hoặc commentId).
     * KHÔNG có @JoinColumn để tránh ràng buộc FK cứng — đây là Polymorphic Association.
     */
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    /**
     * Lý do báo cáo do người dùng nhập.
     * Giới hạn 500 ký tự để tránh spam nội dung dài.
     */
    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    /**
     * Trạng thái xử lý của report:
     *   PENDING   → chờ Admin xem xét
     *   RESOLVED  → Admin xác nhận vi phạm, đã xóa nội dung
     *   DISMISSED → Admin từ chối, báo cáo không hợp lệ
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING"; // default: PENDING

    /** Ghi chú của Admin khi xử lý (tùy chọn). */
    @Column(name = "admin_note", length = 500)
    private String adminNote;

    /** Thời điểm người dùng gửi báo cáo. */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** Thời điểm Admin xử lý (RESOLVED hoặc DISMISSED). Null nếu chưa xử lý. */
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public Report() {}

    public Report(User reporter, String targetType, Long targetId, String reason) {
        this.reporter = reporter;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    // ==================== Getters & Setters ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getReporter() { return reporter; }
    public void setReporter(User reporter) { this.reporter = reporter; }

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
