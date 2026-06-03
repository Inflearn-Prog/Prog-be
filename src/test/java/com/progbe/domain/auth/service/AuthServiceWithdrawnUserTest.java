package com.progbe.domain.auth.service;

import com.progbe.domain.auth.client.SocialApiClient;
import com.progbe.domain.auth.dto.SocialLoginRequest;
import com.progbe.domain.auth.dto.SocialLoginResponse;
import com.progbe.domain.auth.entity.RefreshTokenEntity;
import com.progbe.domain.auth.mapper.AuthMapper;
import com.progbe.domain.auth.repository.RefreshTokenRepository;
import com.progbe.domain.user.dto.UserLoginResult;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.domain.user.repository.UserRepository;
import com.progbe.domain.user.service.UserService;
import com.progbe.domain.user.type.Role;
import com.progbe.domain.user.type.UserStatus;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import com.progbe.global.jwt.JwtTokenProvider;
import com.progbe.global.oauth.OAuth2Attributes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceWithdrawnUserTest {

    @Mock private UserService userService;
    @Mock private UserRepository userRepository;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private AuthMapper authMapper;
    @Mock private SocialApiClient socialApiClient;
    @Mock private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthService authService;

    private UserEntity deletedUser;
    private static final Long USER_ID = 1L;
    private static final String OLD_REFRESH_TOKEN = "old-refresh-token";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenValidityInMilliseconds", 1_209_600_000L);

        deletedUser = UserEntity.builder()
                .nickname("탈퇴유저")
                .email("deleted@example.com")
                .profileUrl("https://example.com/profile.png")
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(deletedUser, "id", USER_ID);
        deletedUser.delete(); // status = DELETED, deletedAt 설정
    }

    @Nested
    @DisplayName("탈퇴 후 토큰 재발급 시도 (refresh)")
    class RefreshAfterWithdrawal {

        @Test
        @DisplayName("탈퇴한 사용자의 refresh token으로 재발급 시도 시 USER_NOT_FOUND를 던진다")
        void refresh_deletedUser_throwsUserNotFound() {
            // given
            RefreshTokenEntity storedToken = RefreshTokenEntity.builder()
                    .userId(USER_ID)
                    .tokenValue(OLD_REFRESH_TOKEN)
                    .expiresAt(LocalDateTime.now().plusDays(7))
                    .deviceInfo("test-device")
                    .build();

            given(jwtTokenProvider.validateToken(OLD_REFRESH_TOKEN)).willReturn(true);
            given(refreshTokenRepository.findByTokenValueForUpdate(OLD_REFRESH_TOKEN))
                    .willReturn(Optional.of(storedToken));
            given(jwtTokenProvider.getSubject(OLD_REFRESH_TOKEN)).willReturn(USER_ID.toString());
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(deletedUser));

            // when & then
            assertThatThrownBy(() -> authService.refresh(OLD_REFRESH_TOKEN, "test-device"))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);

            verify(jwtTokenProvider, never()).createAccessToken(any());
            verify(jwtTokenProvider, never()).createRefreshToken(any());
        }

        @Test
        @DisplayName("이미 무효화된 refresh token으로 재발급 시도 시 예외를 던지고 해당 유저의 모든 토큰을 무효화한다")
        void refresh_revokedToken_revokeAllAndThrows() {
            // given: 이미 revoke된 토큰 (보안 - 토큰 재사용 감지)
            RefreshTokenEntity storedToken = RefreshTokenEntity.builder()
                    .userId(USER_ID)
                    .tokenValue(OLD_REFRESH_TOKEN)
                    .expiresAt(LocalDateTime.now().plusDays(7))
                    .deviceInfo("test-device")
                    .build();
            storedToken.revoke();

            given(jwtTokenProvider.validateToken(OLD_REFRESH_TOKEN)).willReturn(true);
            given(refreshTokenRepository.findByTokenValueForUpdate(OLD_REFRESH_TOKEN))
                    .willReturn(Optional.of(storedToken));

            // when & then
            assertThatThrownBy(() -> authService.refresh(OLD_REFRESH_TOKEN, "test-device"))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_TOKEN);

            // 토큰 재사용 감지 → 해당 유저의 모든 토큰 무효화
            verify(refreshTokenRepository).revokeAllByUserId(USER_ID);
        }
    }

    @Nested
    @DisplayName("탈퇴 후 소셜 재로그인 시도 (socialLogin)")
    class SocialLoginAfterWithdrawal {

        @Test
        @DisplayName("탈퇴한 사용자가 동일 소셜 계정으로 재로그인하면 USER_NOT_FOUND를 던진다")
        void socialLogin_deletedUser_throwsUserNotFound() {
            // given: 탈퇴한 사용자가 동일 소셜 계정으로 재로그인 시도
            // registerOrUpdateUser()가 내부에서 DELETED 체크 후 예외를 던짐
            OAuth2Attributes attrs = OAuth2Attributes.builder()
                    .providerId("kakao_123")
                    .nickname("탈퇴유저")
                    .email("deleted@example.com")
                    .profileImageUrl("https://example.com/img.png")
                    .attributes(Collections.emptyMap())
                    .nameAttributeKey("id")
                    .build();

            given(socialApiClient.getSocialUserInfo("KAKAO", "social-access-token")).willReturn(attrs);
            given(userService.registerOrUpdateUser("KAKAO", attrs, null))
                    .willThrow(new CustomException(ErrorCode.USER_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> authService.socialLogin(
                    new SocialLoginRequest("KAKAO", "social-access-token"), null))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);

            verify(jwtTokenProvider, never()).createAccessToken(any());
            verify(jwtTokenProvider, never()).createRefreshToken(any());
        }
    }
}
