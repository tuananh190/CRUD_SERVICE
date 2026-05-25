package com.mar.CRUD_SERVICE.service;

import com.mar.CRUD_SERVICE.dto.request.ReportRequest;
import com.mar.CRUD_SERVICE.dto.response.ReportResponse;
import com.mar.CRUD_SERVICE.model.Post;
import com.mar.CRUD_SERVICE.model.Comment;
import com.mar.CRUD_SERVICE.model.Report;
import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.repository.CommentRepository;
import com.mar.CRUD_SERVICE.repository.PostRepository;
import com.mar.CRUD_SERVICE.repository.ReportRepository;
import com.mar.CRUD_SERVICE.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service xử lý toàn bộ nghiệp vụ Báo cáo Vi phạm Nội dung (Content Report).
 *
 * =====================================================================
 * NGUYÊN TẮC THIẾT KẾ — LOOSE COUPLING & MODULARITY
 * =====================================================================
 *
 * Module Report được thiết kế độc lập hoàn toàn với luồng CRUD Post/Comment
 * thông qua hai nguyên tắc kiến trúc:
 *
 * 1. LOOSE COUPLING (Khớp nối lỏng lẻo):
 *    ReportService KHÔNG inject PostService hay CommentService làm dependency.
 *    Thay vào đó, khi cần xóa nội dung, nó truy xuất thẳng vào PostRepository
 *    và CommentRepository. Điều này tránh vòng phụ thuộc circular dependency
 *    và đảm bảo PostService không cần biết đến sự tồn tại của ReportService.
 *
 *    Lợi ích: Nếu PostService được refactor hay thay đổi business logic trong
 *    tương lai, ReportService KHÔNG bị ảnh hưởng.
 *
 * 2. ZERO PERFORMANCE IMPACT on Hot Path (Đường dẫn nóng):
 *    "Hot path" = luồng user thường dùng nhất: Đọc bài viết, Comment, React.
 *    Module Report KHÔNG được gọi trong bất kỳ bước nào của hot path này.
 *    Report chỉ được kích hoạt khi user bấm "Báo cáo" (hành động hiếm gặp)
 *    hoặc khi Admin mở dashboard xử lý. Đây là kiến trúc On-Demand, không
 *    phải Eager-Load — không ảnh hưởng latency của các API thông thường.
 *
 * 3. MODULARITY (Tính module hóa):
 *    Toàn bộ tính năng Report được đóng gói trong: Report.java, ReportRepository,
 *    ReportService, ReportController. Có thể tắt hoàn toàn bằng cách bỏ
 *    @Service annotation mà không làm hỏng bất kỳ module nào khác.
 * =====================================================================
 */
@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    // Inject Repository trực tiếp, KHÔNG inject PostService hay CommentService
    // → đây là điểm then chốt của Loose Coupling
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public ReportService(ReportRepository reportRepository,
                         UserRepository userRepository,
                         PostRepository postRepository,
                         CommentRepository commentRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    // ================================================================
    // LUỒNG 1: Người dùng gửi báo cáo
    // Endpoint: POST /api/v1/reports
    // ================================================================

    /**
     * Tiếp nhận và lưu báo cáo vi phạm từ người dùng.
     *
     * Quy trình xử lý:
     * 1. Validate targetType phải là "POST" hoặc "COMMENT"
     * 2. Validate reason không được để trống
     * 3. Kiểm tra target có thực sự tồn tại không
     * 4. Chống spam: một user chỉ report một target một lần (khi còn PENDING)
     * 5. Lưu Report với status = PENDING
     *
     * @param request  DTO chứa targetType, targetId, reason
     * @param username Username của người gửi (từ JWT)
     * @return ReportResponse với thông tin report vừa tạo
     */
    @Transactional
    public ReportResponse submitReport(ReportRequest request, String username) {
        // Bước 1: Validate targetType
        String targetType = normalizeTargetType(request.getTargetType());

        // Bước 2: Validate reason
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new IllegalStateException("Lý do báo cáo không được để trống.");
        }
        if (request.getReason().length() > 500) {
            throw new IllegalStateException("Lý do báo cáo không được vượt quá 500 ký tự.");
        }

        // Bước 3: Validate targetId không null
        if (request.getTargetId() == null) {
            throw new IllegalStateException("targetId không hợp lệ.");
        }

        // Bước 4: Xác minh target tồn tại trong DB
        // Gọi thẳng vào Repository, không qua Service → Loose Coupling
        verifyTargetExists(targetType, request.getTargetId());

        // Bước 5: Lấy thông tin người báo cáo
        User reporter = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + username));

        // Bước 6: Chống spam — kiểm tra đã report target này chưa (khi còn PENDING)
        if (reportRepository.existsPendingReportByReporterAndTarget(reporter, targetType, request.getTargetId())) {
            throw new IllegalStateException("Bạn đã gửi báo cáo cho nội dung này rồi và đang chờ xử lý.");
        }

        // Bước 7: Tạo và lưu Report
        Report report = new Report(reporter, targetType, request.getTargetId(), request.getReason().trim());
        reportRepository.save(report);

        return mapToResponse(report);
    }

    // ================================================================
    // LUỒNG 2: Admin lấy danh sách report
    // Endpoint: GET /api/v1/admin/reports?status=PENDING
    // ================================================================

    /**
     * Lấy danh sách report theo trạng thái cho Admin dashboard.
     *
     * @param status Trạng thái cần lọc: "PENDING", "RESOLVED", "DISMISSED".
     *               Null hoặc "ALL" → lấy toàn bộ theo thứ tự mới nhất.
     * @return Danh sách ReportResponse
     */
    public List<ReportResponse> getReports(String status) {
        List<Report> reports;

        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
            reports = reportRepository.findAllByOrderByCreatedAtDesc();
        } else {
            reports = reportRepository.findByStatusOrderByCreatedAtAsc(status.toUpperCase());
        }

        return reports.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ================================================================
    // LUỒNG 3: Admin xử lý report (phần nghiệp vụ phức tạp nhất)
    // Endpoint: PUT /api/v1/admin/reports/{reportId}/resolve
    // ================================================================

    /**
     * Admin xử lý một báo cáo — đây là hàm nghiệp vụ cốt lõi.
     *
     * Quy trình xử lý theo từng action:
     *
     * [action = "RESOLVE"] — Admin xác nhận vi phạm:
     *   1. Xóa nội dung vi phạm (post hoặc comment) khỏi DB
     *   2. Cập nhật report hiện tại: PENDING → RESOLVED
     *   3. Auto-dismiss các report PENDING trùng lặp cho cùng target
     *      (vì target đã xóa, các report khác không còn ý nghĩa)
     *
     * [action = "DISMISS"] — Admin từ chối báo cáo:
     *   1. Cập nhật report: PENDING → DISMISSED
     *   2. Nội dung KHÔNG bị xóa
     *
     * @param reportId  ID của report cần xử lý
     * @param action    "RESOLVE" (xóa nội dung) hoặc "DISMISS" (bác bỏ)
     * @param adminNote Ghi chú tuỳ chọn của Admin
     * @return ReportResponse với trạng thái đã cập nhật
     */
    @Transactional
    public ReportResponse resolveReport(Long reportId, String action, String adminNote) {
        // Tìm report cần xử lý
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy report ID: " + reportId));

        // Chỉ xử lý được report đang ở trạng thái PENDING
        if (!"PENDING".equals(report.getStatus())) {
            throw new IllegalStateException(
                    "Report này đã được xử lý trước đó. Trạng thái hiện tại: " + report.getStatus()
            );
        }

        String normalizedAction = action != null ? action.toUpperCase().trim() : "";

        if ("RESOLVE".equals(normalizedAction)) {
            // === NHÁNH RESOLVE: Admin xác nhận vi phạm → xóa nội dung ===

            // Bước A: Xóa nội dung vi phạm
            // deleteTarget() gọi thẳng vào Repository → không đi qua PostService/CommentService
            // → tránh trigger validation ownership ("chỉ chủ nhân mới được xóa")
            // → Admin có quyền xóa bất kỳ nội dung nào
            deleteTarget(report.getTargetType(), report.getTargetId());

            // Bước B: Cập nhật report này → RESOLVED
            report.setStatus("RESOLVED");
            report.setAdminNote(adminNote);
            report.setResolvedAt(LocalDateTime.now());
            reportRepository.save(report);

            // Bước C: Auto-dismiss các report PENDING trùng lặp cho cùng target
            // Lý do: nội dung đã xóa → các report khác nhắm vào target đó không còn cần xử lý
            List<Report> duplicates = reportRepository.findPendingByTarget(
                    report.getTargetType(), report.getTargetId()
            );
            for (Report dup : duplicates) {
                // Bỏ qua report hiện tại (đã xử lý ở Bước B)
                if (!dup.getId().equals(report.getId())) {
                    dup.setStatus("DISMISSED");
                    dup.setAdminNote("Tự động đóng: nội dung đã bị xóa do report #" + report.getId() + " được xử lý.");
                    dup.setResolvedAt(LocalDateTime.now());
                    reportRepository.save(dup);
                }
            }

        } else if ("DISMISS".equals(normalizedAction)) {
            // === NHÁNH DISMISS: Admin từ chối → nội dung không bị xóa ===
            report.setStatus("DISMISSED");
            report.setAdminNote(adminNote);
            report.setResolvedAt(LocalDateTime.now());
            reportRepository.save(report);

        } else {
            throw new IllegalStateException("Action không hợp lệ. Chỉ chấp nhận: RESOLVE hoặc DISMISS.");
        }

        return mapToResponse(report);
    }

    // ================================================================
    // PRIVATE HELPER METHODS
    // ================================================================

    /**
     * Chuẩn hóa và validate targetType.
     * Chỉ chấp nhận "POST" hoặc "COMMENT" (case-insensitive).
     */
    private String normalizeTargetType(String targetType) {
        if (targetType == null || targetType.isBlank()) {
            throw new IllegalStateException("targetType không được để trống. Chỉ chấp nhận: POST, COMMENT.");
        }
        String upper = targetType.toUpperCase().trim();
        if (!upper.equals("POST") && !upper.equals("COMMENT")) {
            throw new IllegalStateException("targetType không hợp lệ: '" + targetType + "'. Chỉ chấp nhận: POST, COMMENT.");
        }
        return upper;
    }

    /**
     * Xác minh target tồn tại trong DB trước khi tạo report.
     * Không để user báo cáo một bài viết đã xóa hoặc không tồn tại.
     */
    private void verifyTargetExists(String targetType, Long targetId) {
        if ("POST".equals(targetType)) {
            if (!postRepository.existsById(targetId)) {
                throw new IllegalArgumentException("Bài viết ID " + targetId + " không tồn tại.");
            }
        } else if ("COMMENT".equals(targetType)) {
            if (!commentRepository.existsById(targetId)) {
                throw new IllegalArgumentException("Bình luận ID " + targetId + " không tồn tại.");
            }
        }
    }

    /**
     * Xóa nội dung vi phạm trực tiếp qua Repository.
     *
     * THIẾT KẾ QUAN TRỌNG — LÝ DO KHÔNG GỌI SERVICE.deletePost()/deleteComment():
     * deletePost() trong PostService có kiểm tra ownership:
     *   "if (!post.getAuthor().getUsername().equals(currentUsername)) throw..."
     * Admin không phải chủ bài viết → sẽ bị từ chối.
     *
     * Giải pháp: Gọi thẳng Repository.deleteById() — đây là hành động Admin có
     * đặc quyền, bypassing ownership check một cách có chủ đích (intentional bypass).
     * Admin context được bảo vệ bởi ROLE_ADMIN ở tầng SecurityConfig.
     *
     * Cascade: Post entity có cascade=ALL trên comments → xóa Post sẽ tự động
     * xóa toàn bộ Comment, Reaction liên quan (được cấu hình sẵn trong Post.java).
     */
    private void deleteTarget(String targetType, Long targetId) {
        if ("POST".equals(targetType)) {
            // Kiểm tra tồn tại trước khi xóa để tránh NoSuchElementException
            Post post = postRepository.findById(targetId).orElse(null);
            if (post != null) {
                // CascadeType.ALL trên Post.comments sẽ xóa luôn comment con
                postRepository.deleteById(targetId);
            }
            // Nếu post đã bị xóa trước đó → bỏ qua, không throw exception
        } else if ("COMMENT".equals(targetType)) {
            Comment comment = commentRepository.findById(targetId).orElse(null);
            if (comment != null) {
                commentRepository.deleteById(targetId);
            }
        }
    }

    /**
     * Chuyển đổi Report entity → ReportResponse DTO.
     * Xử lý an toàn trường hợp reporter bị xóa (reporter = null sau fetch).
     */
    private ReportResponse mapToResponse(Report report) {
        ReportResponse response = new ReportResponse();
        response.setId(report.getId());
        // Lấy username từ reporter (có thể null nếu user đã bị xóa)
        response.setReporterUsername(
                report.getReporter() != null ? report.getReporter().getUsername() : "[Đã xóa]"
        );
        response.setTargetType(report.getTargetType());
        response.setTargetId(report.getTargetId());
        response.setReason(report.getReason());
        response.setStatus(report.getStatus());
        response.setAdminNote(report.getAdminNote());
        response.setCreatedAt(report.getCreatedAt());
        response.setResolvedAt(report.getResolvedAt());
        return response;
    }
}
