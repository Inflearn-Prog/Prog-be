package com.progbe.domain.user.mapper;

import com.progbe.domain.user.dto.OnboardingResponse;
import com.progbe.domain.user.dto.UserProfileResponse;
import com.progbe.domain.user.dto.UserWithdrawalResponse;
import com.progbe.domain.user.entity.*;
import com.progbe.domain.user.type.*;
import com.progbe.global.oauth.OAuth2Attributes;
import org.springframework.stereotype.Component;

import java.util.List;
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

    public OnboardingResponse toOnboardingResponse(Long userId, String message, String nextStep) {
        return OnboardingResponse.builder()
                .userId(userId)
                .message(message)
                .nextStep(nextStep)
                .build();
    }

    public UserProfileEntity toUserProfileEntity(UserEntity user) {
        return UserProfileEntity.builder()
                .user(user)
                .build();
    }

    public UserProfileResponse toUserProfileResponse(
            UserEntity user,
            UserProfileEntity profile,
            List<String> experiences
    ) {
        // 1. Basic Info
        String provider = user.getSocialLinks().stream()
                .findFirst() // TODO : 여러 연동 계정 중 첫 번째를 대표로 표시
                .map(UserSocialLinkEntity::getProvider)
                .orElse(null);

        UserProfileResponse.BasicInfo basicInfo = new UserProfileResponse.BasicInfo(
                user.getNickname(),
                user.getEmail(),
                provider,
                profile != null ? profile.getBio() : null
        );

        // 2. Career Info
        List<CareerStatus> currentStatus = (profile != null && profile.getCurrentStatus() != null)
                ? profile.getCurrentStatus() : List.of();

        List<JobRole> targetJob = (profile != null && profile.getTargetJob() != null)
                ? profile.getTargetJob() : List.of();

        String careerYear = "경력 없음";
        if (profile != null && profile.getExperienceYears() != null) {
            careerYear = profile.getExperienceYears() == 0 ? "경력 없음" : profile.getExperienceYears() + "년차";
        }

        EducationLevel education = profile != null ? profile.getEducation() : null;

        UserProfileResponse.CareerInfo careerInfo = new UserProfileResponse.CareerInfo(
                currentStatus,
                targetJob,
                careerYear,
                education
        );

        // 3. Self Intro
        List<String> keywords = (profile != null && profile.getKeywords() != null)
                ? profile.getKeywords()
                : List.of();

        List<String> safeExperiences = experiences != null ? experiences : List.of();

        UserProfileResponse.SelfIntro selfIntro = new UserProfileResponse.SelfIntro(
                safeExperiences,
                keywords
        );

        return new UserProfileResponse(basicInfo, careerInfo, selfIntro);
    }

    public UserExperiencesEntity toUserExperienceEntity(UserEntity user, String description) {
        return UserExperiencesEntity.builder()
                .user(user)
                .description(description)
                .build();
    }
}