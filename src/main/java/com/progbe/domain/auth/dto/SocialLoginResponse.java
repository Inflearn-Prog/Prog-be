package com.progbe.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;

@Builder
public record SocialLoginResponse(
        boolean isNewUser,
        String accessToken,
        @JsonIgnore
        String refreshToken
) {
}