package com.progbe.domain.user.service;

import com.progbe.domain.auth.client.SocialApiClient;
import com.progbe.domain.auth.repository.RefreshTokenRepository;
import com.progbe.domain.user.dto.UserLoginResult;
import com.progbe.domain.user.dto.UserWithdrawalRequest;
import com.progbe.domain.user.dto.UserWithdrawalResponse;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.domain.user.entity.UserSocialLinkEntity;
import com.progbe.domain.user.mapper.UserMapper;
import com.progbe.domain.user.repository.UserRepository;
import com.progbe.domain.user.repository.UserWithdrawalHistoryRepository;
import com.progbe.domain.user.type.Role;
import com.progbe.domain.user.type.UserStatus;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import com.progbe.global.oauth.OAuth2Attributes;
import lombok.RequiredArgsConstructor;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserWithdrawalHistoryRepository userWithdrawalHistoryRepository;
    private final SocialApiClient socialApiClient;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * 소셜 로그인 사용자를 등록하거나 기존 사용자 정보를 업데이트합니다.
     *
     * @param provider   소셜 로그인 제공자 (kakao | naver)
     * @param attributes 소셜 로그인에서 받아온 사용자 정보
     * @return UserLoginResult (UserEntity, isNewUser)
     */
    @Transactional
    public UserLoginResult registerOrUpdateUser(String provider, OAuth2Attributes attributes, String socialRefreshToken) {
        return userRepository.findBySocialProviderAndId(provider, attributes.providerId())
                .map(user -> {
                    if (user.getStatus() == UserStatus.DELETED) {
                        throw new CustomException(ErrorCode.USER_NOT_FOUND);
                    }

                    if (socialRefreshToken != null) {
                        user.getSocialLinks().stream()
                                .filter(link -> link.getProvider().equalsIgnoreCase(provider))
                                .findFirst()
                                .ifPresent(link -> link.updateSocialRefreshToken(socialRefreshToken));
                    }

                    return updateUser(user, attributes);
                })
                .orElseGet(() -> registerUser(provider, attributes, socialRefreshToken));
    }

    private UserLoginResult updateUser(UserEntity user, OAuth2Attributes attributes) {
        user.updateProfile(attributes.nickname(), attributes.profileImageUrl());
        boolean isNewUser = !user.isRegistrationComplete();
        return new UserLoginResult(user, isNewUser);
    }

    private UserLoginResult registerUser(String provider, OAuth2Attributes attributes, String socialRefreshToken) {
        UserEntity newUser = userMapper.toUserEntity(attributes);
        UserSocialLinkEntity socialLink = userMapper.toUserSocialLink(newUser, provider, attributes.providerId(), socialRefreshToken);

        newUser.getSocialLinks().add(socialLink);

        return new UserLoginResult(userRepository.save(newUser), true);
    }

    @Transactional
    public UserWithdrawalResponse withdrawUser(Long userId, UserWithdrawalRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.DELETED) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        // 소셜 연동 해제를 먼저 시도 (실패해도 모든 provider 시도)
        List<String> unlinkFailedProviders = new ArrayList<>();
        for (UserSocialLinkEntity link : user.getSocialLinks()) {
            try {
                String refreshToken = link.getSocialRefreshToken();
                if (refreshToken != null) {
                    String accessToken = socialApiClient.refreshAccessToken(link.getProvider(), refreshToken);
                    if (accessToken != null) {
                        socialApiClient.unlink(link.getProvider(), accessToken);
                    }
                }
            } catch (Exception e) {
                unlinkFailedProviders.add(link.getProvider());
            }
        }

        if (!unlinkFailedProviders.isEmpty()) {
            throw new CustomException(ErrorCode.SOCIAL_UNLINK_FAILED);
        }

        // 연동 해제 성공 후 유저 삭제 처리
        user.delete();
        refreshTokenRepository.revokeAllByUserId(user.getId());

        if (request != null && request.reason() != null) {
            userWithdrawalHistoryRepository.save(
                    userMapper.toWithdrawalHistory(user.getId(), request.reason())
            );
        }

        String providers = user.getSocialLinks().stream()
                .map(UserSocialLinkEntity::getProvider)
                .distinct()
                .reduce((a, b) -> a + "," + b)
                .orElse("NONE");

        return userMapper.toWithdrawalResponse(user, providers);
    }

    // 유저 ID로 유저 전체 엔티티 가져오는 로직
    public UserEntity getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    // 유저 권한 관리자 확인 로직
    public boolean checkAdmin(Long userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return user.getRole().equals(Role.ADMIN);
    }
}