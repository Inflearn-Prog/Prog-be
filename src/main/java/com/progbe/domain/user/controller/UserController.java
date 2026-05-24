package com.progbe.domain.user.controller;

import com.progbe.domain.prompt.dto.PromptListResponse;
import com.progbe.domain.prompt.service.PromptService;
import com.progbe.domain.terms.dto.*;
import com.progbe.domain.terms.service.TermsService;
import com.progbe.domain.user.dto.*;
import com.progbe.domain.user.service.UserProfileService;
import com.progbe.domain.user.service.UserService;
import com.progbe.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    private final PromptService promptService;

    @PostMapping("/terms-agreement")
    public ApiResponse<TermsAgreementResponse> agreeTerms(
            @Valid @RequestBody TermsAgreementRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        TermsAgreementResponse response = termsService.processAgreement(userId, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/me/terms-agreements")
    public ApiResponse<TermsWithdrawalResponse> withdrawTermsAgreement(
            @Valid @RequestBody TermsWithdrawalRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        TermsWithdrawalResponse response = termsService.withdrawTermsAgreement(userId, request);
        return ApiResponse.success(response);
    }

    @GetMapping("/me/terms-agreements")
    public ApiResponse<UserAgreedTermsResponse> getUserAgreedTerms(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        UserAgreedTermsResponse response = termsService.getUserAgreedTerms(userId);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/me")
    public ApiResponse<UserWithdrawalResponse> withdraw(
            @RequestBody(required = false) UserWithdrawalRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());

        UserWithdrawalResponse response = userService.withdrawUser(userId, request);
        return ApiResponse.success(response);
    }

    @PostMapping("/me/onboarding/complete")
    public ApiResponse<OnboardingResponse> completeOnboarding(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        OnboardingResponse response = userProfileService.completeOnboarding(userId);
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

    @GetMapping("/me/profile")
    public ApiResponse<UserProfileResponse> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        UserProfileResponse response = userProfileService.getProfile(userId);
        return ApiResponse.success(response);
    }

    @PatchMapping("/me/profile")
    public ApiResponse<UserProfileUpdateResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        UserProfileUpdateResponse response = userProfileService.updateProfile(userId, request);
        return ApiResponse.success(response);
    }

    @PostMapping("/nickname")
    public ApiResponse<NicknameRegisterResponse> registerNickname(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody NicknameRegisterRequest request
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        NicknameRegisterResponse response = userProfileService.registerNickname(userId, request.nickname());
        return ApiResponse.success(response);
    }

    @GetMapping("/{userId}/liked")
    public ApiResponse<PromptListResponse> getLikedPrompts(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 4) Pageable pageable
    ) {
        Long requestUserId = Long.parseLong(userDetails.getUsername());
        PromptListResponse response = promptService.getLikedPromptsByUser(userId, requestUserId, pageable);
        return ApiResponse.success(response);
    }

    @GetMapping("/{userId}/prompts")
    public ApiResponse<PromptListResponse> getUserPrompts(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 4) Pageable pageable
    ) {
        Long requestUserId = Long.parseLong(userDetails.getUsername());
        PromptListResponse response = promptService.getPromptsByUser(userId, requestUserId, pageable);
        return ApiResponse.success(response);
    }
}
