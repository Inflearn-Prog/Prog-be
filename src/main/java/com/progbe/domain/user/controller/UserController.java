package com.progbe.domain.user.controller;

import com.progbe.domain.terms.dto.TermsAgreementRequest;
import com.progbe.domain.terms.dto.TermsAgreementResponse;
import com.progbe.domain.terms.service.TermsService;
import com.progbe.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final TermsService termsService;

    @PostMapping("/terms-agreement")
    public ApiResponse<TermsAgreementResponse> agreeTerms(
            @RequestBody TermsAgreementRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        
        TermsAgreementResponse response = termsService.processAgreement(userId, request);

        return ApiResponse.success(response);
    }
}
