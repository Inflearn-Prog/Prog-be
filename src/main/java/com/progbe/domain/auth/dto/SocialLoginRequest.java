package com.progbe.domain.auth.dto;

public record SocialLoginRequest(
        String provider, // KAKAO, NAVER
        String accessToken,

        /**
         * 소셜 제공자가 발급한 리프레시 토큰.
         *
         * <p>액세스 토큰은 약 1시간 뒤 만료되므로, 한참 뒤에 일어나는 회원 탈퇴 시점에는
         * 이미 쓸 수 없다. 탈퇴 시 연동 해제(unlink)를 하려면 이 토큰으로 액세스 토큰을
         * 새로 발급받아야 하므로 로그인 시점에 받아 저장한다.
         *
         * <p>제공자가 주지 않으면 null 이며, 그 경우 해당 계정은 연동 해제가 불가능하다.
         */
        String refreshToken
) {
}