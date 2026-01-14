package com.progbe.domain.auth.service;

import com.progbe.domain.auth.client.SocialApiClient;
import com.progbe.domain.auth.dto.SocialLoginRequest;
import com.progbe.domain.auth.dto.SocialLoginResponse;
import com.progbe.domain.auth.dto.TokenResponse;
import com.progbe.domain.auth.mapper.AuthMapper;
import com.progbe.domain.user.dto.SocialTokens;
import com.progbe.domain.user.dto.UserLoginResult;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthMapper authMapper;
    private final SocialApiClient socialApiClient;

    @Transactional
    public SocialLoginResponse socialLogin(SocialLoginRequest request) {
        String provider = request.provider().toUpperCase();

        SocialTokens socialTokens = socialApiClient.getSocialTokens(provider, request.authCode());

        OAuth2Attributes oAuth2Attributes = socialApiClient.getSocialUserInfo(provider, socialTokens.accessToken());

        UserLoginResult loginResult = userService.registerOrUpdateUser(
                provider,
                oAuth2Attributes,
                socialTokens.refreshToken()
        );

        UserEntity userEntity = loginResult.user();
        boolean isNewUser = loginResult.isNewUser();

        UserDetails principal = new User(userEntity.getId().toString(), "",
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + userEntity.getRole().name())));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());

        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        return authMapper.toSocialLoginResponse(isNewUser, accessToken, refreshToken);
    }

    public TokenResponse refresh(String oldRefreshToken) {
        if (oldRefreshToken == null || !jwtTokenProvider.validateToken(oldRefreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        String userId = jwtTokenProvider.getSubject(oldRefreshToken);
        UserEntity user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Authentication authentication = createAuthentication(user);

        String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication);

        return authMapper.toTokenResponse(newAccessToken, newRefreshToken);
    }

    private Authentication createAuthentication(UserEntity user) {
        UserDetails principal = new org.springframework.security.core.userdetails.User(
                user.getId().toString(),
                "",
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
        return new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());
    }
}