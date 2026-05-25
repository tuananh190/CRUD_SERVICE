package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.dto.request.ReportRequest;
import com.mar.CRUD_SERVICE.dto.response.ReportResponse;
import com.mar.CRUD_SERVICE.service.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Controller xử lý Báo cáo Vi phạm Nội dung (Content Report).
 *
 * Phân chia endpoint thành 2 nhóm rõ ràng:
 *
 * [NHÓM USER]  /api/v1/reports       — User gửi báo cáo
 * [NHÓM ADMIN] /api/v1/admin/reports  — Admin xem và xử lý báo cáo
 *
 * Phân quyền:
 * - /api/v1/reports/**        → .authenticated() (đã cấu hình anyRequest trong SecurityConfig)
 * - /api/v1/admin/reports/**  → .hasRole("ADMIN") (cần thêm vào SecurityConfig — xem hướng dẫn bên dưới)
 */
@RestController
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // ================================================================
    // [NHÓM USER] — Tất cả user đã đăng nhập
    // ================================================================

    /**
     * POST /api/v1/reports
     * Gửi báo cáo vi phạm lên một bài viết hoặc bình luận.
     *
     * Request body:
     * {
     *   "targetType": "POST",   ← hoặc "COMMENT"
     *   "targetId": 42,
     *   "reason": "Nội dung bạo lực"
     * }
     *
     * Postman test:
     * - POST /api/v1/reports + Authorization: Bearer <user_token>
     * - Body (raw JSON): {"targetType":"POST","targetId":1,"reason":"Spam"}
     * - Kỳ vọng: 201 Created với ReportResponse
     */
    @PostMapping("/api/v1/reports")
    public ResponseEntity<ReportResponse> submitReport(@RequestBody ReportRequest request,
                                                       Principal principal) {
        try {
            ReportResponse response = reportService.submitReport(request, principal.getName());
            // Trả về 201 Created thay vì 200 OK — đúng ngữ nghĩa REST khi tạo resource mới
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            // 409 Conflict khi đã report rồi, hoặc 400 khi input không hợp lệ
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalArgumentException e) {
            // 404 khi target (post/comment) không tồn tại
            return ResponseEntity.notFound().build();
        }
    }

    // ================================================================
    // [NHÓM ADMIN] — Chỉ dành cho ROLE_ADMIN
    // Bảo vệ bởi SecurityConfig: .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
    // ================================================================

    /**
     * GET /api/v1/admin/reports?status=PENDING
     * Lấy danh sách báo cáo để Admin xem xét.
     *
     * Query param ?status=:
     * - "PENDING"   → chỉ report chưa xử lý
     * - "RESOLVED"  → report đã xử lý (đã xóa nội dung)
     * - "DISMISSED" → report đã bác bỏ
     * - "ALL" hoặc không truyền → toàn bộ
     *
     * Postman test:
     * - GET /api/v1/admin/reports?status=PENDING + Authorization: Bearer <admin_token>
     * - Kỳ vọng: 200 OK với danh sách report PENDING
     */
    @GetMapping("/api/v1/admin/reports")
    public ResponseEntity<List<ReportResponse>> getReports(
            @RequestParam(value = "status", required = false, defaultValue = "PENDING") String status) {
        List<ReportResponse> reports = reportService.getReports(status);
        return ResponseEntity.ok(reports);
    }

    /**
     * PUT /api/v1/admin/reports/{reportId}/resolve
     * Admin xử lý một báo cáo cụ thể.
     *
     * Request body:
     * {
     *   "action": "RESOLVE",    ← Xác nhận vi phạm → xóa nội dung
     *   "adminNote": "Đã kiểm tra, nội dung vi phạm điều khoản sử dụng."
     * }
     * hoặc:
     * {
     *   "action": "DISMISS",    ← Bác bỏ báo cáo → nội dung không bị xóa
     *   "adminNote": "Nội dung không vi phạm."
     * }
     *
     * Postman test (kịch bản RESOLVE):
     * - PUT /api/v1/admin/reports/1/resolve + Authorization: Bearer <admin_token>
     * - Body: {"action":"RESOLVE","adminNote":"Xóa do vi phạm"}
     * - Kỳ vọng: 200 OK, bài viết bị report bị xóa, status chuyển RESOLVED
     *
     * Postman test (kịch bản DISMISS):
     * - Body: {"action":"DISMISS","adminNote":"Không vi phạm"}
     * - Kỳ vọng: 200 OK, nội dung vẫn còn, status chuyển DISMISSED
     */
    @PutMapping("/api/v1/admin/reports/{reportId}/resolve")
    public ResponseEntity<ReportResponse> resolveReport(
            @PathVariable Long reportId,
            @RequestBody Map<String, String> body) {
        try {
            // Lấy "action" và "adminNote" từ request body dạng Map<String, String>
            // Dùng Map thay vì DTO riêng để giữ code gọn — chỉ 2 field đơn giản
            String action = body.get("action");
            String adminNote = body.get("adminNote"); // có thể null — OK vì không bắt buộc

            ReportResponse response = reportService.resolveReport(reportId, action, adminNote);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            // Report đã được xử lý rồi, hoặc action không hợp lệ
            return ResponseEntity.badRequest().build();
        } catch (IllegalArgumentException e) {
            // Report ID không tồn tại
            return ResponseEntity.notFound().build();
        }
    }
}
