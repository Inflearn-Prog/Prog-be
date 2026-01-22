package com.progbe.domain.user.service;

import com.progbe.domain.user.dto.OnboardingBasicRequest;
import com.progbe.domain.user.dto.OnboardingCareerRequest;
import com.progbe.domain.user.dto.OnboardingResponse;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.domain.user.entity.UserProfileEntity;
import com.progbe.domain.user.mapper.UserMapper;
import com.progbe.domain.user.repository.UserProfileRepository;
import com.progbe.domain.user.repository.UserRepository;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public OnboardingResponse saveCareerInfo(Long userId, OnboardingCareerRequest request) {
        UserProfileEntity userProfile = getOrNewUserProfile(userId);

        userProfile.updateCareerInfo(request.currentStatuses(), request.targetJobRoles());
        userProfileRepository.save(userProfile);

        return userMapper.toOnboardingResponse(userId, "커리어 정보가 성공적으로 저장되었습니다.", "CAREER_DETAILS");
    }

    @Transactional
    public OnboardingResponse saveBasicInfo(Long userId, OnboardingBasicRequest request) {
        UserProfileEntity userProfile = getOrNewUserProfile(userId);

        userProfile.updateBasicInfo(request.educationLevel(), request.major(), request.careerYears());
        userProfileRepository.save(userProfile);

        return userMapper.toOnboardingResponse(userId, "기본 정보가 저장되었습니다.", "CAREER_INFO");
    }

    private UserProfileEntity getOrNewUserProfile(Long userId) {
        return userProfileRepository.findById(userId)
                .orElseGet(() -> {
                    UserEntity user = userRepository.findById(userId)
                            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
                    return userMapper.toUserProfileEntity(user);
                });
    }

    @Transactional(readOnly = true)
    public com.progbe.domain.user.dto.UserProfileResponse getProfile(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 온보딩 과정에서 모든 필드가 입력되지 않은 경우 -> null 처리
        UserProfileEntity userProfile = userProfileRepository.findById(userId).orElse(null);

        return userMapper.toUserProfileResponse(user, userProfile);
    }
}
