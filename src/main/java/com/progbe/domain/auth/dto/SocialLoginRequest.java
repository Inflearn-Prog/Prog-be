package com.progbe.domain.auth.dto;

public record SocialLoginRequest(
        String provider, // KAKAO, NAVER
        String accessToken
) {
}