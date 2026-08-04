package com.progbe.domain.auth.dto;

public record SocialLoginRequest(
        String provider, // KAKAO, NAVER
        String accessToken,
        String refreshToken // 탈퇴 시 연동 해제(unlink)에 사용. 제공자가 주지 않으면 null
) {
}