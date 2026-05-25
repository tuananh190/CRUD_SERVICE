package com.mar.CRUD_SERVICE.repository;

import com.mar.CRUD_SERVICE.model.Report;
import com.mar.CRUD_SERVICE.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository truy vấn bảng "reports".
 *
 * Tất cả query đều giới hạn trong bảng "reports" duy nhất —
 * KHÔNG có JOIN sang bảng posts/comments/users để đảm bảo
 * module Report hoàn toàn độc lập (Loose Coupling).
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    /**
     * Lấy danh sách report theo trạng thái — dùng cho Admin dashboard.
     * Index idx_report_status đảm bảo query này chạy O(log n).
     *
     * Ví dụ sử dụng: findByStatus("PENDING") → danh sách report chờ xử lý.
     */
    List<Report> findByStatusOrderByCreatedAtAsc(String status);

    /**
     * Lấy toàn bộ report theo thứ tự mới nhất — Admin xem lịch sử đầy đủ.
     */
    List<Report> findAllByOrderByCreatedAtDesc();

    /**
     * Kiểm tra user đã report một target cụ thể chưa.
     * Mục đích: Chống spam — một user chỉ được report một bài/comment một lần.
     *
     * @param reporter   User đang thực hiện báo cáo
     * @param targetType Loại nội dung ("POST" hoặc "COMMENT")
     * @param targetId   ID của nội dung
     * @return true nếu đã tồn tại report từ user này cho target này
     */
    @Query("SELECT COUNT(r) > 0 FROM Report r WHERE r.reporter = :reporter AND r.targetType = :targetType AND r.targetId = :targetId AND r.status = 'PENDING'")
    boolean existsPendingReportByReporterAndTarget(
            @Param("reporter") User reporter,
            @Param("targetType") String targetType,
            @Param("targetId") Long targetId
    );

    /**
     * Lấy tất cả report PENDING nhắm vào một target cụ thể.
     * Dùng khi Admin RESOLVE một report — cần dismiss các report trùng lặp khác
     * cho cùng target (vì nội dung đã bị xóa, không cần xử lý thêm).
     */
    @Query("SELECT r FROM Report r WHERE r.targetType = :targetType AND r.targetId = :targetId AND r.status = 'PENDING'")
    List<Report> findPendingByTarget(
            @Param("targetType") String targetType,
            @Param("targetId") Long targetId
    );
}
