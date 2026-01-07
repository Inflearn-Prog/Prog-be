package com.progbe.domain.auth.component;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AuthCookieManager {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    private static final long REFRESH_TOKEN_EXPIRATION = 14 * 24 * 60 * 60;

    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(false) // TODO : SSL 적용 후 true로 변경 필요
                .path("/")
                .maxAge(REFRESH_TOKEN_EXPIRATION)
                .sameSite("None")
                .build();
    }
}