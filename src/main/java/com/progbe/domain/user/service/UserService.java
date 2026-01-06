package com.progbe.domain.user.service;

import com.progbe.domain.user.dto.UserLoginResult;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.domain.user.entity.UserSocialLink;
import com.progbe.domain.user.mapper.UserMapper;
import com.progbe.domain.user.repository.UserRepository;
import com.progbe.global.oauth.OAuth2Attributes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * 소셜 로그인 사용자를 등록하거나 기존 사용자 정보를 업데이트합니다.
     *
     * @param provider   소셜 로그인 제공자 (kakao | naver)
     * @param attributes 소셜 로그인에서 받아온 사용자 정보
     * @return UserLoginResult (UserEntity, isNewUser)
     */
    @Transactional
    public UserLoginResult registerOrUpdateUser(String provider, OAuth2Attributes attributes) {
        return userRepository.findBySocialProviderAndId(provider, attributes.providerId())
                .map(user -> updateUser(user, attributes))
                .orElseGet(() -> registerUser(provider, attributes));
    }

    private UserLoginResult updateUser(UserEntity user, OAuth2Attributes attributes) {
        user.updateProfile(attributes.nickname(), attributes.profileImageUrl());
        return new UserLoginResult(user, false);
    }

    private UserLoginResult registerUser(String provider, OAuth2Attributes attributes) {
        UserEntity newUser = userMapper.toUserEntity(attributes);
        UserSocialLink socialLink = userMapper.toUserSocialLink(newUser, provider, attributes.providerId());

        newUser.getSocialLinks().add(socialLink);

        return new UserLoginResult(userRepository.save(newUser), true);
    }
}