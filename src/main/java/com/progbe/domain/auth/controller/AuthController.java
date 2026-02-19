package com.progbe.domain.auth.controller;

import com.progbe.domain.auth.component.AuthCookieManager;
import com.progbe.domain.auth.component.DeviceInfoExtractor;
import com.progbe.domain.auth.dto.SocialLoginRequest;
import com.progbe.domain.auth.dto.SocialLoginResponse;
import com.progbe.domain.auth.dto.TokenResponse;
import com.progbe.domain.auth.service.AuthService;
import com.progbe.global.common.ApiResponse;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthCookieManager authCookieManager;
    private final DeviceInfoExtractor deviceInfoExtractor;

    @PostMapping("/social-login")
    public ResponseEntity<SocialLoginResponse> socialLogin(
            @Valid @RequestBody SocialLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        String deviceInfo = deviceInfoExtractor.extractDeviceInfo(httpRequest);
        SocialLoginResponse loginResult = authService.socialLogin(request, deviceInfo);

        ResponseCookie cookie = authCookieManager.createRefreshTokenCookie(loginResult.refreshToken());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(loginResult);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<String>> refresh(
            @CookieValue(value = "refresh_token", required = false) String refreshToken,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            log.debug("리프레시 토큰이 없거나 공백입니다.");
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        String deviceInfo = deviceInfoExtractor.extractDeviceInfo(httpRequest);
        TokenResponse tokenDto = authService.refresh(refreshToken, deviceInfo);

        ResponseCookie cookie = authCookieManager.createRefreshTokenCookie(tokenDto.refreshToken());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(ApiResponse.success(tokenDto.accessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(value = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        authService.logout(refreshToken);

        ResponseCookie cookie = authCookieManager.createDeleteCookie();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAllDevices(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletResponse response
    ) {
        Long userId = Long.valueOf(userDetails.getUsername());
        authService.logoutAllDevices(userId);

        ResponseCookie cookie = authCookieManager.createDeleteCookie();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}