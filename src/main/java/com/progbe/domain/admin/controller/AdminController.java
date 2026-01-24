package com.progbe.domain.admin.controller;

import com.progbe.domain.admin.dto.AdminResponse;
import com.progbe.domain.admin.service.AdminService;
import com.progbe.domain.terms.dto.TermsListResponse;
import com.progbe.domain.terms.service.TermsService;
import com.progbe.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController
{
    private final AdminService adminService;

    @GetMapping("/statistics/daily-summary")
    public ResponseEntity<ApiResponse<AdminResponse.DailyStatisticSummaryResponse>> getDailyStatisticSummary() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getDailyStatisticSummary()));
    }

    @GetMapping("/user/search")
    public ResponseEntity<ApiResponse<AdminResponse.UserSearchResult>> searchUsers(
            @RequestParam String keyword,
            @RequestParam int page
    ) {
        return ResponseEntity.ok(ApiResponse.success(adminService.searchUsers(keyword,page)));
    }
}
