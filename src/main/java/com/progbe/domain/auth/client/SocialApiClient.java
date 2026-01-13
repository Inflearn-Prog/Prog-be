package com.progbe.domain.auth.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.progbe.domain.user.dto.SocialTokens;
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

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SocialApiClient {

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper;

    // KAKAO Configuration
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String kakaoClientSecret;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String kakaoTokenUri;

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String kakaoUserInfoUri;

    // NAVER Configuration
    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String naverClientId;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String naverClientSecret;

    private static final String NAVER_STATE = "STATE_STRING";

    @Value("${spring.security.oauth2.client.provider.naver.token-uri}")
    private String naverTokenUri;

    @Value("${spring.security.oauth2.client.provider.naver.user-info-uri}")
    private String naverUserInfoUri;

    /**
     * Authorization Code로 Social Access/Refresh Token 발급
     */
    public SocialTokens getSocialTokens(String provider, String authCode) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", authCode);

        String tokenUri = getProviderTokenUri(provider, body);

        String response = restClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(String.class);

        return parseTokens(response);
    }

    /**
     * Refresh Token으로 Social Access Token 갱신
     */
    public String refreshAccessToken(String provider, String refreshToken) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", refreshToken);

        String tokenUri = getProviderTokenUri(provider, body);

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
            log.warn("Failed to refresh access token for provider: {}", provider, e);
            return null;
        }
    }

    /**
     * Social Access Token으로 사용자 정보 조회
     */
    public OAuth2Attributes getSocialUserInfo(String provider, String accessToken) {
        String userInfoUri;
        String providerId;

        if ("KAKAO".equals(provider)) {
            userInfoUri = kakaoUserInfoUri;
            providerId = "kakao";
        } else if ("NAVER".equals(provider)) {
            userInfoUri = naverUserInfoUri;
            providerId = "naver";
        } else {
            throw new CustomException(ErrorCode.INVALID_PROVIDER);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> attributes = restClient.get()
                .uri(userInfoUri)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        return OAuth2Attributes.of(providerId, attributes);
    }

    /**
     * 소셜 연동 해제
     */
    public void unlink(String provider, String accessToken) {
        String unlinkUri;

        if ("KAKAO".equals(provider)) {
            unlinkUri = "https://kapi.kakao.com/v1/user/unlink";
        } else if ("NAVER".equals(provider)) {
            // 네이버는 연동 해제 시 delete 토큰 요청 필요
            unlinkUri = naverTokenUri + "?grant_type=delete&client_id=" + naverClientId +
                    "&client_secret=" + naverClientSecret + "&access_token=" + accessToken + "&service_provider=NAVER";
        } else {
            return;
        }

        try {
            restClient.post()
                    .uri(unlinkUri)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Social unlink success: {}", provider);
        } catch (Exception e) {
            log.error("Failed to unlink social account: {}", provider, e);
        }
    }

    private String getProviderTokenUri(String provider, MultiValueMap<String, String> body) {
        if ("KAKAO".equals(provider)) {
            body.add("client_id", kakaoClientId);
            body.add("client_secret", kakaoClientSecret);
            body.add("redirect_uri", kakaoRedirectUri);
            return kakaoTokenUri;
        } else if ("NAVER".equals(provider)) {
            body.add("client_id", naverClientId);
            body.add("client_secret", naverClientSecret);
            body.add("state", NAVER_STATE);
            return naverTokenUri;
        } else {
            throw new CustomException(ErrorCode.INVALID_PROVIDER);
        }
    }

    private SocialTokens parseTokens(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            String accessToken = jsonNode.get("access_token").asText();
            String refreshToken = jsonNode.has("refresh_token") ? jsonNode.get("refresh_token").asText() : null;
            return new SocialTokens(accessToken, refreshToken);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }
    }
}