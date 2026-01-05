package com.progbe.domain.user.service;

import com.progbe.domain.user.dto.UserLoginResult;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.domain.user.entity.UserSocialLink;
import com.progbe.domain.user.repository.UserRepository;
import com.progbe.domain.user.type.Role;
import com.progbe.domain.user.type.UserStatus;
import com.progbe.global.oauth.OAuth2Attributes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 소셜 로그인 사용자를 등록하거나 기존 사용자 정보를 업데이트합니다.
     *
     * @param provider 소셜 로그인 제공자 (kakao | naver)
     * @param attributes 소셜 로그인에서 받아온 사용자 정보
     * @return UserLoginResult (UserEntity, isNewUser)
     */
    @Transactional
    public UserLoginResult registerOrUpdateUser(String provider, OAuth2Attributes attributes) {
        return userRepository.findBySocialProviderAndId(provider, attributes.providerId())
                .map(entity -> {
                    entity.updateProfile(attributes.nickname(), attributes.profileImageUrl());
                    return new UserLoginResult(entity, false);
                })
                .orElseGet(() -> {
                    UserEntity newUserEntity = UserEntity.builder()
                            .nickname(attributes.nickname() + "_" + UUID.randomUUID().toString().substring(0, 5))
                            .email(attributes.email())
                            .profileUrl(attributes.profileImageUrl())
                            .role(Role.USER)
                            .status(UserStatus.ACTIVE)
                            .build();

                    UserSocialLink socialLink = UserSocialLink.builder()
                            .userEntity(newUserEntity)
                            .provider(provider)
                            .providerUserId(attributes.providerId())
                            .build();

                    newUserEntity.getSocialLinks().add(socialLink);
                    UserEntity savedUser = userRepository.save(newUserEntity);

                    return new UserLoginResult(savedUser, true);
                });
    }
}