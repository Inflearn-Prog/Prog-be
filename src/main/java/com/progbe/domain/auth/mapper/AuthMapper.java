package com.progbe.domain.auth.mapper;

import com.progbe.domain.auth.dto.SocialLoginResponse;
import com.progbe.domain.auth.dto.TokenResponse;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public SocialLoginResponse toSocialLoginResponse(boolean isNewUser, String accessToken, String refreshToken) {
        return SocialLoginResponse.builder()
                .isNewUser(isNewUser)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public TokenResponse toTokenResponse(String accessToken, String refreshToken) {
        return new TokenResponse(accessToken, refreshToken);
    }
}