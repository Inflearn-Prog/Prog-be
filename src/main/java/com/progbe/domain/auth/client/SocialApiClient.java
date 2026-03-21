package com.progbe.domain.auth.client;

import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import com.progbe.global.oauth.OAuth2Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SocialApiClient {

    private final List<SocialClient> socialClients;

    public String refreshAccessToken(String provider, String refreshToken) {
        return getClient(provider).refreshAccessToken(refreshToken);
    }

    public OAuth2Attributes getSocialUserInfo(String provider, String accessToken) {
        return getClient(provider).getSocialUserInfo(accessToken);
    }

    public void unlink(String provider, String accessToken) {
        getClient(provider).unlink(accessToken);
    }

    private SocialClient getClient(String provider) {
        return socialClients.stream()
                .filter(client -> client.supports(provider))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_PROVIDER));
    }
}