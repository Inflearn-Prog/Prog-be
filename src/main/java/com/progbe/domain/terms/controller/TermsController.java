package com.progbe.domain.terms.controller;

import com.progbe.domain.terms.dto.TermsListResponse;
import com.progbe.domain.terms.service.TermsService;
import com.progbe.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/terms")
@RequiredArgsConstructor
public class TermsController {

    private final TermsService termsService;

    @GetMapping
    public ResponseEntity<ApiResponse<TermsListResponse>> getTerms() {
        TermsListResponse response = termsService.getAllTerms();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
