package com.progbe.domain.admin.controller;

import com.progbe.domain.admin.dto.AdminRequest;
import com.progbe.domain.admin.dto.AdminResponse;
import com.progbe.domain.admin.service.AdminService;
import com.progbe.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    // FIXME - start ============================
    @GetMapping("/user/search")
    public ResponseEntity<ApiResponse<AdminResponse.UserSearchResult>> searchUsers(
            @RequestParam String keyword,
            @RequestParam int page
    ) {
        return ResponseEntity.ok(ApiResponse.success(adminService.searchUsers(keyword, page)));
    }
    // FIXME - end ============================

    // 기존의 statistics/daily-summary 대체 로직
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
}
