package com.progbe.domain.admin.controller;

import com.progbe.domain.admin.dto.AdminPromptRequest;
import com.progbe.domain.admin.dto.AdminPromptResponse;
import com.progbe.domain.admin.dto.AdminRequest;
import com.progbe.domain.admin.dto.AdminResponse;
import com.progbe.domain.admin.dto.AdminUserRequest;
import com.progbe.domain.admin.dto.AdminUserResponse;
import com.progbe.domain.admin.service.AdminPromptService;
import com.progbe.domain.admin.service.AdminService;
import com.progbe.domain.admin.service.AdminUserService;
import com.progbe.domain.prompt.type.PromptStatus;
import com.progbe.domain.report.dto.PendingReportListResponse;
import com.progbe.domain.report.dto.ReportProcessRequest;
import com.progbe.domain.report.dto.ReportProcessResponse;
import com.progbe.domain.report.service.AdminReportService;
import com.progbe.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final AdminUserService adminUserService;
    private final AdminPromptService adminPromptService;
    private final AdminReportService adminReportService;

    @GetMapping("/stats/summary")
    public ResponseEntity<ApiResponse<AdminResponse.StatsSummaryResponse>> getStatsSummary(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        AdminRequest.StatsSummaryRequest request = new AdminRequest.StatsSummaryRequest(startDate, endDate);
        AdminResponse.StatsSummaryResponse response = adminService.getStatsSummary(
                request.startDate(),
                request.endDate()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<AdminUserResponse.UserListResponse>> getUserList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        AdminUserResponse.UserListResponse response = adminUserService.getUserList(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/users/bulk-status")
    public ResponseEntity<ApiResponse<AdminUserResponse.BulkUpdateResponse>> bulkUpdateStatus(
            @Valid @RequestBody AdminUserRequest.BulkStatusUpdateRequest request
    ) {
        AdminUserResponse.BulkUpdateResponse response = adminUserService.bulkUpdateStatus(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/users/bulk-role")
    public ResponseEntity<ApiResponse<AdminUserResponse.BulkUpdateResponse>> bulkUpdateRole(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AdminUserRequest.BulkRoleUpdateRequest request
    ) {
        Long currentUserId = Long.parseLong(userDetails.getUsername());
        AdminUserResponse.BulkUpdateResponse response = adminUserService.bulkUpdateRole(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/prompts")
    public ResponseEntity<ApiResponse<AdminPromptResponse.PromptListResponse>> getPromptList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) PromptStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        AdminPromptResponse.PromptListResponse response = adminPromptService.getPromptList(
                keyword,
                categoryId,
                status,
                page,
                size
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/prompts/bulk-update")
    public ResponseEntity<ApiResponse<AdminPromptResponse.BulkUpdateResponse>> bulkUpdatePrompts(
            @Valid @RequestBody AdminPromptRequest.BulkUpdateRequest request
    ) {
        AdminPromptResponse.BulkUpdateResponse response = adminPromptService.bulkUpdatePrompts(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/prompts/bulk")
    public ResponseEntity<ApiResponse<AdminPromptResponse.BulkDeleteResponse>> bulkDeletePrompts(
            @Valid @RequestBody AdminPromptRequest.BulkDeleteRequest request
    ) {
        AdminPromptResponse.BulkDeleteResponse response = adminPromptService.bulkDeletePrompts(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/reports/pending")
    public ResponseEntity<ApiResponse<PendingReportListResponse>> getPendingReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        PendingReportListResponse response = adminReportService.getPendingReports(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/reports/{reportId}/process")
    public ResponseEntity<ApiResponse<ReportProcessResponse>> processReport(
            @PathVariable Long reportId,
            @Valid @RequestBody ReportProcessRequest request
    ) {
        ReportProcessResponse response = adminReportService.processReport(reportId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
