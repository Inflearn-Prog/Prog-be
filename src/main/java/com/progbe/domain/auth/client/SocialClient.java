package com.progbe.domain.auth.client;

import com.progbe.global.oauth.OAuth2Attributes;

public interface SocialClient {
    boolean supports(String provider);
    String refreshAccessToken(String refreshToken);
    OAuth2Attributes getSocialUserInfo(String accessToken);
    void unlink(String accessToken);
}