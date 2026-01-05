package com.progbe.domain.auth.dto;

public record SocialLoginRequestDto(
        String provider, // KAKAO, NAVER
        String authCode
) {
}