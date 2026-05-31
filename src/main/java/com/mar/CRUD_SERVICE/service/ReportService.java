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

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

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

    @Transactional
    public ReportResponse submitReport(ReportRequest request, String username) {

        String targetType = normalizeTargetType(request.getTargetType());

        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new IllegalStateException("Lý do báo cáo không được để trống.");
        }
        if (request.getReason().length() > 500) {
            throw new IllegalStateException("Lý do báo cáo không được vượt quá 500 ký tự.");
        }

        if (request.getTargetId() == null) {
            throw new IllegalStateException("targetId không hợp lệ.");
        }

        verifyTargetExists(targetType, request.getTargetId());

        User reporter = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + username));

        if (reportRepository.existsPendingReportByReporterAndTarget(reporter, targetType, request.getTargetId())) {
            throw new IllegalStateException("Bạn đã gửi báo cáo cho nội dung này rồi và đang chờ xử lý.");
        }

        Report report = new Report(reporter, targetType, request.getTargetId(), request.getReason().trim());
        reportRepository.save(report);

        return mapToResponse(report);
    }

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

    @Transactional
    public ReportResponse resolveReport(Long reportId, String action, String adminNote) {

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy report ID: " + reportId));

        if (!"PENDING".equals(report.getStatus())) {
            throw new IllegalStateException(
                    "Report này đã được xử lý trước đó. Trạng thái hiện tại: " + report.getStatus()
            );
        }

        String normalizedAction = action != null ? action.toUpperCase().trim() : "";

        if ("RESOLVE".equals(normalizedAction)) {

            deleteTarget(report.getTargetType(), report.getTargetId());

            report.setStatus("RESOLVED");
            report.setAdminNote(adminNote);
            report.setResolvedAt(LocalDateTime.now());
            reportRepository.save(report);

            List<Report> duplicates = reportRepository.findPendingByTarget(
                    report.getTargetType(), report.getTargetId()
            );
            for (Report dup : duplicates) {

                if (!dup.getId().equals(report.getId())) {
                    dup.setStatus("DISMISSED");
                    dup.setAdminNote("Tự động đóng: nội dung đã bị xóa do report #" + report.getId() + " được xử lý.");
                    dup.setResolvedAt(LocalDateTime.now());
                    reportRepository.save(dup);
                }
            }

        } else if ("DISMISS".equals(normalizedAction)) {

            report.setStatus("DISMISSED");
            report.setAdminNote(adminNote);
            report.setResolvedAt(LocalDateTime.now());
            reportRepository.save(report);

        } else {
            throw new IllegalStateException("Action không hợp lệ. Chỉ chấp nhận: RESOLVE hoặc DISMISS.");
        }

        return mapToResponse(report);
    }

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

    private void deleteTarget(String targetType, Long targetId) {
        if ("POST".equals(targetType)) {

            Post post = postRepository.findById(targetId).orElse(null);
            if (post != null) {

                postRepository.deleteById(targetId);
            }

        } else if ("COMMENT".equals(targetType)) {
            Comment comment = commentRepository.findById(targetId).orElse(null);
            if (comment != null) {
                commentRepository.deleteById(targetId);
            }
        }
    }

    private ReportResponse mapToResponse(Report report) {
        ReportResponse response = new ReportResponse();
        response.setId(report.getId());

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
