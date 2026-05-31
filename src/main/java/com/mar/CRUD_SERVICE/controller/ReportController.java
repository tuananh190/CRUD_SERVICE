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

@RestController
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/api/v1/reports")
    public ResponseEntity<ReportResponse> submitReport(@RequestBody ReportRequest request,
                                                       Principal principal) {
        try {
            ReportResponse response = reportService.submitReport(request, principal.getName());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {

            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        } catch (IllegalArgumentException e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/api/v1/admin/reports")
    public ResponseEntity<List<ReportResponse>> getReports(
            @RequestParam(value = "status", required = false, defaultValue = "PENDING") String status) {
        List<ReportResponse> reports = reportService.getReports(status);
        return ResponseEntity.ok(reports);
    }

    @PutMapping("/api/v1/admin/reports/{reportId}/resolve")
    public ResponseEntity<ReportResponse> resolveReport(
            @PathVariable Long reportId,
            @RequestBody Map<String, String> body) {
        try {

            String action = body.get("action");
            String adminNote = body.get("adminNote");

            ReportResponse response = reportService.resolveReport(reportId, action, adminNote);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {

            return ResponseEntity.badRequest().body(null);
        } catch (IllegalArgumentException e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
