package com.progbe.domain.user.service;

import com.progbe.domain.user.dto.*;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.domain.user.entity.UserExperiencesEntity;
import com.progbe.domain.user.entity.UserProfileEntity;
import com.progbe.domain.user.mapper.UserMapper;
import com.progbe.domain.user.repository.UserExperiencesRepository;
import com.progbe.domain.user.repository.UserProfileRepository;
import com.progbe.domain.user.repository.UserRepository;
import com.progbe.domain.user.type.CareerStatus;
import com.progbe.domain.user.type.EducationLevel;
import com.progbe.domain.user.type.JobRole;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    // TODO : 우선은 한/영/숫자만 허용. 정책 확인 필요
    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z0-9]{1,12}$");
    private static final int NICKNAME_FREE_CHANGE_LIMIT = 2;
    private static final int NICKNAME_CHANGE_COOLDOWN_HOURS = 24;

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final UserExperiencesRepository userExperiencesRepository;
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
    public UserProfileResponse getProfile(Long userId) {
        UserEntity user = userRepository.findByIdWithSocialLinks(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 온보딩 과정에서 모든 필드가 입력되지 않은 경우 -> null 처리
        UserProfileEntity userProfile = userProfileRepository.findById(userId).orElse(null);

        List<String> experiences = userExperiencesRepository.findByUserOrderByCreatedAtAsc(user)
                .stream()
                .map(UserExperiencesEntity::getDescription)
                .toList();

        return userMapper.toUserProfileResponse(user, userProfile, experiences);
    }

    @Transactional
    public UserProfileUpdateResponse updateProfile(Long userId, UserProfileUpdateRequest request) {
        UserEntity user = userRepository.findByIdWithSocialLinks(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        UserProfileEntity profile = getOrNewUserProfile(userId);

        updateBasicInfo(user, profile, request.basicInfo());
        updateCareerInfo(profile, request.careerInfo());
        updateSelfIntro(user, profile, request.selfIntro());

        userProfileRepository.save(profile);

        return new UserProfileUpdateResponse("프로필 정보가 성공적으로 저장되었습니다.");
    }

    @Transactional
    public NicknameRegisterResponse registerNickname(Long userId, String nickname) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        try {
            validateAndChangeNickname(user, nickname);
            return new NicknameRegisterResponse("닉네임이 성공적으로 등록되었습니다.");
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.NICKNAME_ALREADY_USED);
        }
    }


    private void updateBasicInfo(UserEntity user, UserProfileEntity profile,
                                 UserProfileUpdateRequest.BasicInfo basicInfo) {
        if (basicInfo == null) {
            return;
        }

        // 닉네임은 빈 문자열을 허용하지 않음
        if (basicInfo.nickname() != null) {
            String nickname = basicInfo.nickname().trim();
            if (nickname.isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_NICKNAME_FORMAT);
            }
            handleNicknameChange(user, nickname);
        }

        // 자기소개(bio)
        if (basicInfo.introduction() != null) {
            String intro = basicInfo.introduction().trim();
            if (intro.isEmpty()) {
                profile.updateBio(null);
            } else {
                profile.updateBio(intro);
            }
        }
    }

    private void handleNicknameChange(UserEntity user, String nickname) {
        if (nickname.equals(user.getNickname())) {
            return;
        }

        try {
            validateAndChangeNickname(user, nickname);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.NICKNAME_ALREADY_USED);
        }
    }

    private void validateAndChangeNickname(UserEntity user, String nickname) {
        String trimmedNickname = nickname.trim();

        if (trimmedNickname.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_NICKNAME_FORMAT);
        }

        if (!NICKNAME_PATTERN.matcher(trimmedNickname).matches()) {
            throw new CustomException(ErrorCode.INVALID_NICKNAME_FORMAT);
        }

        if (userRepository.existsByNickname(trimmedNickname)) {
            throw new CustomException(ErrorCode.NICKNAME_ALREADY_USED);
        }

        LocalDateTime now = LocalDateTime.now();
        Integer currentChangeCount = user.getNicknameChangeCount();

        if (currentChangeCount >= NICKNAME_FREE_CHANGE_LIMIT) {
            LocalDateTime lastChangedAt = user.getLastNicknameChangedAt();
            if (lastChangedAt != null && lastChangedAt.plusHours(NICKNAME_CHANGE_COOLDOWN_HOURS).isAfter(now)) {
                throw new CustomException(ErrorCode.NICKNAME_CHANGE_TOO_FREQUENT);
            }
        }

        user.changeNicknameManually(trimmedNickname, now);
    }

    private void updateCareerInfo(UserProfileEntity profile,
                                  UserProfileUpdateRequest.CareerInfo careerInfo) {
        if (careerInfo == null) {
            return;
        }

        // currentStatus, targetJob
        List<CareerStatus> currentStatus = profile.getCurrentStatus();
        if (careerInfo.currentStatus() != null) {
            currentStatus = careerInfo.currentStatus();
        }

        List<JobRole> targetJob = profile.getTargetJob();
        if (careerInfo.targetJob() != null) {
            targetJob = careerInfo.targetJob();
        }

        profile.updateCareerInfo(currentStatus, targetJob);

        // education, major, careerYear
        EducationLevel education = profile.getEducation();
        if (careerInfo.education() != null) {
            education = careerInfo.education();
        }

        String major = profile.getMajor();
        if (careerInfo.major() != null) {
            String trimmed = careerInfo.major().trim();
            major = trimmed.isEmpty() ? null : trimmed;
        }

        Integer experienceYears = profile.getExperienceYears();
        experienceYears = careerInfo.careerYear();

        profile.updateBasicInfo(education, major, experienceYears);
    }

    private void updateSelfIntro(UserEntity user, UserProfileEntity profile,
                                 UserProfileUpdateRequest.SelfIntro selfIntro) {
        if (selfIntro == null) {
            return;
        }

        // keywords
        if (selfIntro.keywords() != null) {
            List<String> keywords = selfIntro.keywords().stream()
                    .filter(kw -> kw != null && !kw.trim().isEmpty())
                    .toList();
            profile.updateKeywords(keywords);
        }

        // experiences
        if (selfIntro.experiences() != null) {
            List<String> experiences = selfIntro.experiences().stream()
                    .filter(exp -> exp != null && !exp.trim().isEmpty())
                    .map(String::trim)
                    .peek(exp -> {
                        if (exp.length() > 200) {
                            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
                        }
                    })
                    .toList();

            userExperiencesRepository.deleteByUser(user);
            experiences.forEach(exp ->
                    userExperiencesRepository.save(
                            userMapper.toUserExperienceEntity(user, exp)
                    )
            );
        }
    }
}
