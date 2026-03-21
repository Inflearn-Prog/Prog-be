package com.progbe.domain.auth.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import com.progbe.global.oauth.OAuth2Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoSocialClient implements SocialClient {

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String userInfoUri;

    @Override
    public boolean supports(String provider) {
        return "KAKAO".equalsIgnoreCase(provider);
    }

    @Override
    public String refreshAccessToken(String refreshToken) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", refreshToken);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        try {
            String response = restClient.post()
                    .uri(tokenUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            log.warn("Failed to refresh access token for KAKAO", e);
            return null;
        }
    }

    @Override
    public OAuth2Attributes getSocialUserInfo(String accessToken) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> attributes = restClient.get()
                    .uri(userInfoUri)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);

            return OAuth2Attributes.of("kakao", attributes);
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            log.warn("KAKAO access token expired or invalid", e);
            throw new CustomException(ErrorCode.ACCESS_TOKEN_EXPIRED);
        } catch (Exception e) {
            log.error("Failed to get KAKAO user info", e);
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }
    }

    @Override
    public void unlink(String accessToken) {
        try {
            restClient.post()
                    .uri("https://kapi.kakao.com/v1/user/unlink")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Social unlink success: KAKAO");
        } catch (Exception e) {
            log.error("Failed to unlink social account: KAKAO", e);
        }
    }
}