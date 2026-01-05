package com.progbe.domain.auth.controller;

import com.progbe.domain.auth.dto.SocialLoginRequestDto;
import com.progbe.domain.auth.dto.SocialLoginResponseDto;
import com.progbe.domain.auth.dto.TokenResponseDto;
import com.progbe.domain.auth.service.AuthService;
import com.progbe.global.common.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/social-login")
    public ResponseEntity<SocialLoginResponseDto> socialLogin(
            @RequestBody SocialLoginRequestDto request,
            HttpServletResponse response
    ) {
        SocialLoginResponseDto loginResult = authService.socialLogin(request);
        setRefreshTokenCookie(response, loginResult.refreshToken());

        return ResponseEntity.ok(loginResult);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<String>> refresh(
            @CookieValue(value = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        TokenResponseDto tokenDto = authService.refresh(refreshToken);
        setRefreshTokenCookie(response, tokenDto.refreshToken());

        return ResponseEntity.ok(ApiResponse.success(tokenDto.accessToken()));
    }

    // 쿠키 설정 공통 메서드
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(false) // TODO : SSL 이후 true
                .path("/")
                .maxAge(14 * 24 * 60 * 60)
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}