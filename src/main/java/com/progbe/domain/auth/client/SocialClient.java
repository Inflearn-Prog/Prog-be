package com.progbe.domain.auth.client;

import com.progbe.domain.user.dto.SocialTokens;
import com.progbe.global.oauth.OAuth2Attributes;

public interface SocialClient {
    boolean supports(String provider);
    SocialTokens getSocialTokens(String authCode);
    String refreshAccessToken(String refreshToken);
    OAuth2Attributes getSocialUserInfo(String accessToken);
    void unlink(String accessToken);
}