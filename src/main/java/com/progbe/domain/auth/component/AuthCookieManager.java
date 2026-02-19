package com.progbe.domain.auth.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AuthCookieManager {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenValidityInMilliseconds;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.cookie.same-site:Lax}")
    private String cookieSameSite;

    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        long maxAgeSeconds = refreshTokenValidityInMilliseconds / 1000;
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite(cookieSameSite)
                .build();
    }

    public ResponseCookie createDeleteCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite(cookieSameSite)
                .build();
    }
}