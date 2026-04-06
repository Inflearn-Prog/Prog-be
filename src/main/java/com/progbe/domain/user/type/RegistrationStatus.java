package com.progbe.domain.user.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RegistrationStatus {
    SOCIAL_LOGIN_ONLY("소셜 로그인만 완료"),
    TERMS_AGREED("약관 동의 완료"),
    NICKNAME_REGISTERED("닉네임 등록 완료"),
    CAREER_INFO_COMPLETED("커리어 정보 입력 완료"),
    ONBOARDING_COMPLETED("온보딩 완료");

    private final String description;
}
