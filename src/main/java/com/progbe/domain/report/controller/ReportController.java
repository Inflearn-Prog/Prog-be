package com.progbe.domain.report.controller;

import com.progbe.domain.report.dto.ReportCreateRequest;
import com.progbe.domain.report.dto.ReportCreateResponse;
import com.progbe.domain.report.service.ReportService;
import com.progbe.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReportCreateResponse>> createReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReportCreateRequest request
    ) {
        Long reporterId = Long.parseLong(userDetails.getUsername());
        ReportCreateResponse response = reportService.createReport(reporterId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "201"));
    }
}
