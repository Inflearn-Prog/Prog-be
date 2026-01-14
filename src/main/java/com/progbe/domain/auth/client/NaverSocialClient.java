package com.progbe.domain.auth.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.progbe.domain.auth.utils.TokenParser;
import com.progbe.domain.user.dto.SocialTokens;
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
public class NaverSocialClient implements SocialClient {

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper;

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.provider.naver.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.provider.naver.user-info-uri}")
    private String userInfoUri;

    private static final String NAVER_STATE = "STATE_STRING";

    @Override
    public boolean supports(String provider) {
        return "NAVER".equalsIgnoreCase(provider);
    }

    @Override
    public SocialTokens getSocialTokens(String authCode) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", authCode);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("state", NAVER_STATE);

        String response = restClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(String.class);

        return TokenParser.parseTokens(response, objectMapper);
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
            log.warn("Failed to refresh access token for NAVER", e);
            return null;
        }
    }

    @Override
    public OAuth2Attributes getSocialUserInfo(String accessToken) {
        @SuppressWarnings("unchecked")
        Map<String, Object> attributes = restClient.get()
                .uri(userInfoUri)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Map.class);

        return OAuth2Attributes.of("naver", attributes);
    }

    @Override
    public void unlink(String accessToken) {
        String unlinkUri = tokenUri + "?grant_type=delete&client_id=" + clientId +
                "&client_secret=" + clientSecret + "&access_token=" + accessToken + "&service_provider=NAVER";

        try {
            restClient.post()
                    .uri(unlinkUri)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Social unlink success: NAVER");
        } catch (Exception e) {
            log.error("Failed to unlink social account: NAVER", e);
        }
    }
}