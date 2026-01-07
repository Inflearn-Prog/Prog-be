package com.progbe.domain.auth.controller;

import com.progbe.domain.auth.component.AuthCookieManager;
import com.progbe.domain.auth.dto.SocialLoginRequest;
import com.progbe.domain.auth.dto.SocialLoginResponse;
import com.progbe.domain.auth.dto.TokenResponse;
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
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthCookieManager authCookieManager;

    @PostMapping("/social-login")
    public ResponseEntity<SocialLoginResponse> socialLogin(
            @RequestBody SocialLoginRequest request,
            HttpServletResponse response
    ) {
        SocialLoginResponse loginResult = authService.socialLogin(request);

        ResponseCookie cookie = authCookieManager.createRefreshTokenCookie(loginResult.refreshToken());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(loginResult);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<String>> refresh(
            @CookieValue(value = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        TokenResponse tokenDto = authService.refresh(refreshToken);

        ResponseCookie cookie = authCookieManager.createRefreshTokenCookie(tokenDto.refreshToken());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(ApiResponse.success(tokenDto.accessToken()));
    }
}