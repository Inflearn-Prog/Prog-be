package com.progbe.domain.auth.service;

import com.progbe.domain.auth.client.SocialApiClient;
import com.progbe.domain.auth.dto.SocialLoginRequest;
import com.progbe.domain.auth.dto.SocialLoginResponse;
import com.progbe.domain.auth.dto.TokenResponse;
import com.progbe.domain.auth.entity.RefreshTokenEntity;
import com.progbe.domain.auth.mapper.AuthMapper;
import com.progbe.domain.auth.repository.RefreshTokenRepository;
import com.progbe.domain.user.dto.UserLoginResult;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.domain.user.repository.UserRepository;
import com.progbe.domain.user.service.UserService;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import com.progbe.global.jwt.JwtTokenProvider;
import com.progbe.global.oauth.OAuth2Attributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenValidityInMilliseconds;

    @Transactional
    public SocialLoginResponse socialLogin(SocialLoginRequest request, String deviceInfo) {
        String provider = request.provider().toUpperCase();

        OAuth2Attributes oAuth2Attributes = socialApiClient.getSocialUserInfo(provider, request.accessToken());

        UserLoginResult loginResult = userService.registerOrUpdateUser(
                provider,
                oAuth2Attributes,
                null
        );

        UserEntity userEntity = loginResult.user();
        boolean isNewUser = loginResult.isNewUser();

        UserDetails principal = new User(userEntity.getId().toString(), "",
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + userEntity.getRole().name())));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());

        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        if (deviceInfo != null && !deviceInfo.isBlank()) {
            revokeAndSaveToken(userEntity.getId(), refreshToken, deviceInfo);
        } else {
            saveRefreshToken(userEntity.getId(), refreshToken, deviceInfo);
        }

        return authMapper.toSocialLoginResponse(isNewUser, accessToken, refreshToken);
    }

    private void revokeAndSaveToken(Long userId, String newTokenValue, String deviceInfo) {
        refreshTokenRepository.revokeByUserIdAndDeviceInfo(userId, deviceInfo);
        saveRefreshToken(userId, newTokenValue, deviceInfo);
    }

    @Transactional
    public TokenResponse refresh(String oldRefreshToken, String deviceInfo) {
        if (oldRefreshToken == null || !jwtTokenProvider.validateToken(oldRefreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        RefreshTokenEntity storedToken = refreshTokenRepository.findByTokenValueForUpdate(oldRefreshToken)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));

        if (storedToken.isRevoked()) {
            log.warn("토큰 재사용 감지 - userId: {}, tokenId: {}", storedToken.getUserId(), storedToken.getId());
            refreshTokenRepository.revokeAllByUserId(storedToken.getUserId());
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        if (storedToken.isCurrentlyExpired()) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        String userId = jwtTokenProvider.getSubject(oldRefreshToken);
        UserEntity user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Authentication authentication = createAuthentication(user);

        String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication);

        storedToken.revoke();

        // [Refactor] 단순화
        String finalDeviceInfo = (deviceInfo != null && !deviceInfo.equals("Unknown Device"))
                ? deviceInfo
                : storedToken.getDeviceInfo();
        saveRefreshToken(user.getId(), newRefreshToken, finalDeviceInfo);

        return authMapper.toTokenResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null) {
            refreshTokenRepository.revokeByTokenValue(refreshToken);
        }
    }

    @Transactional
    public void logoutAllDevices(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    private void saveRefreshToken(Long userId, String tokenValue, String deviceInfo) {
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(refreshTokenValidityInMilliseconds / 1000);

        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                .userId(userId)
                .tokenValue(tokenValue)
                .expiresAt(expiresAt)
                .deviceInfo(deviceInfo)
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    private Authentication createAuthentication(UserEntity user) {
        UserDetails principal = new org.springframework.security.core.userdetails.User(
                user.getId().toString(),
                "",
                user.getRole().getAuthorities()
        );
        return new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());
    }
}