package com.progbe.domain.auth.dto;

import lombok.Builder;

@Builder
public record SocialLoginResponseDto(
        boolean isNewUser,
        String accessToken,
        String refreshToken
) {
}