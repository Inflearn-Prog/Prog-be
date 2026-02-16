package com.progbe.domain.admin.controller;

import com.progbe.domain.admin.dto.AdminRequest;
import com.progbe.domain.admin.dto.AdminResponse;
import com.progbe.domain.admin.dto.AdminUserRequest;
import com.progbe.domain.admin.dto.AdminUserResponse;
import com.progbe.domain.admin.service.AdminService;
import com.progbe.domain.admin.service.AdminUserService;
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

    // 기존의 /user/search 대체
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
}
