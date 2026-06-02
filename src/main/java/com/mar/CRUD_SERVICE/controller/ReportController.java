package com.mar.CRUD_SERVICE.controller;

import com.mar.CRUD_SERVICE.dto.response.ApiResponse;

import com.mar.CRUD_SERVICE.dto.request.ReportRequest;
import com.mar.CRUD_SERVICE.dto.response.ReportResponse;
import com.mar.CRUD_SERVICE.service.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/api/v1/reports")
    public ResponseEntity<ApiResponse<ReportResponse>> submitReport(@RequestBody ReportRequest request,
                                                       Principal principal) {
        ReportResponse response = reportService.submitReport(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(201, "Tạo báo cáo thành công", response));
    }

    @GetMapping("/api/v1/admin/reports")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getReports(
            @RequestParam(value = "status", required = false, defaultValue = "PENDING") String status) {
        List<ReportResponse> reports = reportService.getReports(status);
        return ResponseEntity.ok(new ApiResponse<>(200, "Lấy danh sách báo cáo thành công", reports));
    }

    @PutMapping("/api/v1/admin/reports/{reportId}/resolve")
    public ResponseEntity<ApiResponse<ReportResponse>> resolveReport(
            @PathVariable Long reportId,
            @RequestBody Map<String, String> body) {
        String action = body.get("action");
        String adminNote = body.get("adminNote");
        ReportResponse response = reportService.resolveReport(reportId, action, adminNote);
        return ResponseEntity.ok(new ApiResponse<>(200, "Xử lý báo cáo thành công", response));
    }
}
