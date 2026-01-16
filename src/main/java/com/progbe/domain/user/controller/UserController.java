package com.progbe.domain.user.controller;

import com.progbe.domain.terms.dto.TermsAgreementRequest;
import com.progbe.domain.terms.dto.TermsAgreementResponse;
import com.progbe.domain.terms.service.TermsService;
import com.progbe.domain.user.dto.*;
import com.progbe.domain.user.service.UserProfileService;
import com.progbe.domain.user.service.UserService;
import com.progbe.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final TermsService termsService;
    private final UserService userService;
    private final UserProfileService userProfileService;

    @PostMapping("/terms-agreement")
    public ApiResponse<TermsAgreementResponse> agreeTerms(
            @RequestBody TermsAgreementRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        TermsAgreementResponse response = termsService.processAgreement(userId, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/me")
    public ApiResponse<UserWithdrawalResponse> withdraw(
            @PathVariable String uid,
            @RequestBody(required = false) UserWithdrawalRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());

        UserWithdrawalResponse response = userService.withdrawUser(userId, request);
        return ApiResponse.success(response);
    }

    @PutMapping("/me/onboarding/career")
    public ApiResponse<OnboardingResponse> updateCareerInfo(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody OnboardingCareerRequest request
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        OnboardingResponse response = userProfileService.saveCareerInfo(userId, request);
        return ApiResponse.success(response);
    }

    @PutMapping("/me/onboarding/basic")
    public ApiResponse<OnboardingResponse> updateBasicInfo(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody OnboardingBasicRequest request
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        OnboardingResponse response = userProfileService.saveBasicInfo(userId, request);
        return ApiResponse.success(response);
    }
}
