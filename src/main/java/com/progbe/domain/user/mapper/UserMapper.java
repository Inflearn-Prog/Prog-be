package com.progbe.domain.user.mapper;

import com.progbe.domain.user.dto.UserWithdrawalResponse;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.domain.user.entity.UserSocialLinkEntity;
import com.progbe.domain.user.entity.UserWithdrawalHistoryEntity;
import com.progbe.domain.user.type.Role;
import com.progbe.domain.user.type.UserStatus;
import com.progbe.global.oauth.OAuth2Attributes;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserMapper {

    public UserEntity toUserEntity(OAuth2Attributes attributes) {
        String randomSuffix = UUID.randomUUID().toString().substring(0, 5);

        return UserEntity.builder()
                .nickname(attributes.nickname() + "_" + randomSuffix)
                .email(attributes.email())
                .profileUrl(attributes.profileImageUrl())
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    public UserSocialLinkEntity toUserSocialLink(UserEntity user, String provider, String providerUserId, String socialRefreshToken) {
        return UserSocialLinkEntity.builder()
                .userEntity(user)
                .provider(provider)
                .providerUserId(providerUserId)
                .socialRefreshToken(socialRefreshToken)
                .build();
    }

    public UserWithdrawalHistoryEntity toWithdrawalHistory(Long userId, String reason) {
        return UserWithdrawalHistoryEntity.builder()
                .userId(userId)
                .reason(reason)
                .build();
    }

    public UserWithdrawalResponse toWithdrawalResponse(UserEntity user, String unlinkedProviders) {
        return new UserWithdrawalResponse(
                user.getNickname(),
                unlinkedProviders,
                user.getDeletedAt()
        );
    }
}