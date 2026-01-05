package com.progbe.domain.auth.dto;

import lombok.Builder;

@Builder
public record SocialLoginResponse(
        boolean isNewUser,
        String accessToken,
        String refreshToken
) {
}