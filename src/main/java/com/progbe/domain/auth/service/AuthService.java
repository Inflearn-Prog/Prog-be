package com.progbe.domain.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.progbe.domain.auth.dto.SocialLoginRequestDto;
import com.progbe.domain.auth.dto.SocialLoginResponseDto;
import com.progbe.domain.auth.dto.TokenResponseDto;
import com.progbe.domain.user.dto.UserLoginResultDto;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.domain.user.repository.UserRepository;
import com.progbe.domain.user.service.UserService;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import com.progbe.global.jwt.JwtTokenProvider;
import com.progbe.global.oauth.OAuth2Attributes;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.Map;

@SuppressWarnings("unused")
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
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

    @Transactional
    public SocialLoginResponseDto socialLogin(SocialLoginRequestDto request) {
        String provider = request.provider().toUpperCase();

        String socialAccessToken = getSocialAccessToken(provider, request.authCode());

        OAuth2Attributes oAuth2Attributes = getSocialUserInfo(provider, socialAccessToken);

        UserLoginResultDto loginResult = userService.registerOrUpdateUser(provider, oAuth2Attributes);
        UserEntity userEntity = loginResult.user();
        boolean isNewUser = loginResult.isNewUser();

        UserDetails principal = new org.springframework.security.core.userdetails.User(userEntity.getId().toString(), "",
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + userEntity.getRole().name())));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());

        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        return SocialLoginResponseDto.builder()
                .isNewUser(isNewUser)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }


    public TokenResponseDto refresh(String oldRefreshToken) {
        if (oldRefreshToken == null || !jwtTokenProvider.validateToken(oldRefreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        String userId = jwtTokenProvider.getSubject(oldRefreshToken);
        UserEntity user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Authentication authentication = createAuthentication(user);

        String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication);

        return new TokenResponseDto(newAccessToken, newRefreshToken);
    }

    private Authentication createAuthentication(UserEntity user) {
        UserDetails principal = new org.springframework.security.core.userdetails.User(
                user.getId().toString(),
                "",
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
        return new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());
    }

    private String getSocialAccessToken(String provider, String authCode) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", authCode);

        String tokenUri;

        switch (provider) {
            case "KAKAO" -> {
                body.add("client_id", kakaoClientId);
                body.add("client_secret", kakaoClientSecret);
                body.add("redirect_uri", kakaoRedirectUri);
                tokenUri = kakaoTokenUri;
            }
            case "NAVER" -> {
                body.add("client_id", naverClientId);
                body.add("client_secret", naverClientSecret);
                body.add("state", NAVER_STATE);
                tokenUri = naverTokenUri;
            }
            default -> throw new CustomException(ErrorCode.INVALID_PROVIDER);
        }

        String response = restClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(String.class);

        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("access_token").asText();
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }
    }

    private OAuth2Attributes getSocialUserInfo(String provider, String accessToken) {
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
}
