package com.progbe.domain.terms.controller;

import com.progbe.domain.terms.dto.TermsResponseDto;
import com.progbe.domain.terms.service.TermsService;
import com.progbe.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/terms")
@RequiredArgsConstructor
public class TermsController {

    private final TermsService termsService;

    @GetMapping
    public ApiResponse<Map<String, List<TermsResponseDto>>> getTerms() {
        List<TermsResponseDto> terms = termsService.getAllTerms();
        return ApiResponse.success(Map.of("terms", terms));
    }
}
