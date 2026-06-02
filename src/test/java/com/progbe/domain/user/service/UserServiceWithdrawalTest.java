package com.progbe.domain.user.service;

import com.progbe.domain.auth.client.SocialApiClient;
import com.progbe.domain.auth.repository.RefreshTokenRepository;
import com.progbe.domain.user.dto.UserWithdrawalRequest;
import com.progbe.domain.user.dto.UserWithdrawalResponse;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.domain.user.entity.UserSocialLinkEntity;
import com.progbe.domain.user.entity.UserWithdrawalHistoryEntity;
import com.progbe.domain.user.mapper.UserMapper;
import com.progbe.domain.user.repository.UserRepository;
import com.progbe.domain.user.repository.UserWithdrawalHistoryRepository;
import com.progbe.domain.user.type.Role;
import com.progbe.domain.user.type.UserStatus;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceWithdrawalTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private UserWithdrawalHistoryRepository userWithdrawalHistoryRepository;
    @Mock private SocialApiClient socialApiClient;
    @Mock private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private UserService userService;

    private UserEntity activeUser;
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        activeUser = UserEntity.builder()
                .nickname("testUser")
                .email("test@prog.com")
                .profileUrl("https://prog.com/profile.png")
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(activeUser, "id", USER_ID);
    }

    @Nested
    @DisplayName("withdrawUser")
    class WithdrawUser {

        @Test
        @DisplayName("소셜 연동 해제 성공 후 status가 DELETED로 바뀌고 refresh token이 전부 무효화된다")
        void withdraw_success_deletesUserAndRevokesTokens() {
            // given
            UserSocialLinkEntity link = UserSocialLinkEntity.builder()
                    .userEntity(activeUser)
                    .provider("KAKAO")
                    .providerUserId("kakao_123")
                    .socialRefreshToken("social-rt")
                    .build();
            activeUser.getSocialLinks().add(link);

            given(userRepository.findById(USER_ID)).willReturn(Optional.of(activeUser));
            given(socialApiClient.refreshAccessToken("KAKAO", "social-rt")).willReturn("access-token");

            UserWithdrawalHistoryEntity historyEntity = UserWithdrawalHistoryEntity.builder()
                    .userId(USER_ID).reason("서비스 불만족").build();
            given(userMapper.toWithdrawalHistory(USER_ID, "서비스 불만족")).willReturn(historyEntity);

            UserWithdrawalResponse mockResponse = new UserWithdrawalResponse(
                    "testUser", "KAKAO", LocalDateTime.now());
            given(userMapper.toWithdrawalResponse(any(), any())).willReturn(mockResponse);

            // when
            UserWithdrawalResponse response = userService.withdrawUser(USER_ID, new UserWithdrawalRequest("서비스 불만족"));

            // then
            assertThat(activeUser.getStatus()).isEqualTo(UserStatus.DELETED);
            verify(refreshTokenRepository).revokeAllByUserId(USER_ID);
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("이미 탈퇴한 사용자가 재탈퇴 시도 시 USER_NOT_FOUND를 반환하고 refresh token은 건드리지 않는다")
        void withdraw_alreadyDeleted_throwsUserNotFound() {
            // given
            activeUser.delete();
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(activeUser));

            // when & then
            assertThatThrownBy(() -> userService.withdrawUser(USER_ID, null))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);

            verify(refreshTokenRepository, never()).revokeAllByUserId(any());
        }

        @Test
        @DisplayName("존재하지 않는 사용자 탈퇴 시도 시 USER_NOT_FOUND를 던진다")
        void withdraw_userNotFound_throwsUserNotFound() {
            // given
            given(userRepository.findById(USER_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.withdrawUser(USER_ID, null))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("socialRefreshToken이 null이면 unlink를 건너뛰고 탈퇴를 정상 처리한다")
        void withdraw_noSocialRefreshToken_skipsUnlinkAndDeletes() {
            // given: 소셜 링크는 있지만 refresh token이 없는 경우 (최초 로그인 후 토큰 미저장.. 등)
            UserSocialLinkEntity link = UserSocialLinkEntity.builder()
                    .userEntity(activeUser)
                    .provider("KAKAO")
                    .providerUserId("kakao_123")
                    .socialRefreshToken(null)
                    .build();
            activeUser.getSocialLinks().add(link);

            given(userRepository.findById(USER_ID)).willReturn(Optional.of(activeUser));
            UserWithdrawalResponse mockResponse = new UserWithdrawalResponse(
                    "testUser", "KAKAO", LocalDateTime.now());
            given(userMapper.toWithdrawalResponse(any(), any())).willReturn(mockResponse);

            // when
            userService.withdrawUser(USER_ID, null);

            // then: unlink 미호출이지만 탈퇴 및 토큰 무효화는 정상 진행
            verify(socialApiClient, never()).refreshAccessToken(any(), any());
            verify(socialApiClient, never()).unlink(any(), any());
            verify(refreshTokenRepository).revokeAllByUserId(USER_ID);
            assertThat(activeUser.getStatus()).isEqualTo(UserStatus.DELETED);
        }

        @Test
        @DisplayName("소셜 access token 갱신 실패(null 반환) 시 unlink를 건너뛰지만 탈퇴는 진행된다")
        void withdraw_socialAccessTokenRefreshFailed_skipsUnlinkButDeletes() {
            // given: refresh token은 있지만 갱신 요청이 실패(만료 등)해서 null 반환
            UserSocialLinkEntity link = UserSocialLinkEntity.builder()
                    .userEntity(activeUser)
                    .provider("NAVER")
                    .providerUserId("naver_123")
                    .socialRefreshToken("expired-social-rt")
                    .build();
            activeUser.getSocialLinks().add(link);

            given(userRepository.findById(USER_ID)).willReturn(Optional.of(activeUser));
            given(socialApiClient.refreshAccessToken("NAVER", "expired-social-rt")).willReturn(null);
            UserWithdrawalResponse mockResponse = new UserWithdrawalResponse(
                    "testUser", "NAVER", LocalDateTime.now());
            given(userMapper.toWithdrawalResponse(any(), any())).willReturn(mockResponse);

            // when
            userService.withdrawUser(USER_ID, null);

            // then: 소셜 연동은 외부에 유지되지만 DB에서는 탈퇴 처리됨
            verify(socialApiClient, never()).unlink(any(), any());
            verify(refreshTokenRepository).revokeAllByUserId(USER_ID);
            assertThat(activeUser.getStatus()).isEqualTo(UserStatus.DELETED);
        }
    }
}
