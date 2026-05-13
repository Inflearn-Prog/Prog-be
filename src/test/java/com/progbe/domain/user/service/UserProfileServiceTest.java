package com.progbe.domain.user.service;

import com.progbe.domain.user.dto.*;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.domain.user.entity.UserExperiencesEntity;
import com.progbe.domain.user.entity.UserProfileEntity;
import com.progbe.domain.user.mapper.UserMapper;
import com.progbe.domain.user.repository.UserExperiencesRepository;
import com.progbe.domain.user.repository.UserProfileRepository;
import com.progbe.domain.user.repository.UserRepository;
import com.progbe.domain.user.type.*;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserExperiencesRepository userExperiencesRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserProfileService userProfileService;

    private UserEntity testUser;
    private UserProfileEntity testProfile;
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .nickname("testUser")
                .email("test@example.com")
                .profileUrl("https://example.com/profile.png")
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();
        ReflectionTestUtils.setField(testUser, "id", USER_ID);

        testProfile = UserProfileEntity.builder()
                .user(testUser)
                .currentStatus(List.of(CareerStatus.STUDENT))
                .targetJob(List.of(JobRole.DEVELOPMENT))
                .education(EducationLevel.BACHELOR)
                .experienceYears(3)
                .bio("자기소개입니다.")
                .keywords(List.of("Java", "Spring"))
                .build();
    }

    // =========================================================================
    // saveCareerInfo
    // =========================================================================
    @Nested
    @DisplayName("saveCareerInfo")
    class SaveCareerInfo {

        @Test
        @DisplayName("정상 케이스 - 커리어 정보를 저장하고 OnboardingResponse를 반환한다")
        void saveCareerInfo_success() {
            // given
            ReflectionTestUtils.setField(testUser, "registrationStatus", RegistrationStatus.NICKNAME_REGISTERED);

            OnboardingCareerRequest request = new OnboardingCareerRequest(
                    List.of(CareerStatus.JOB_SEEKER),
                    List.of(JobRole.DEVELOPMENT, JobRole.DESIGN)
            );

            OnboardingResponse expectedResponse = OnboardingResponse.builder()
                    .userId(USER_ID)
                    .message("커리어 정보가 성공적으로 저장되었습니다.")
                    .nextStep("CAREER_DETAILS")
                    .build();

            given(userRepository.findById(USER_ID)).willReturn(Optional.of(testUser));
            given(userProfileRepository.findById(USER_ID)).willReturn(Optional.of(testProfile));
            given(userProfileRepository.save(any(UserProfileEntity.class))).willReturn(testProfile);
            given(userMapper.toOnboardingResponse(USER_ID, "커리어 정보가 성공적으로 저장되었습니다.", "CAREER_DETAILS"))
                    .willReturn(expectedResponse);

            // when
            OnboardingResponse result = userProfileService.saveCareerInfo(USER_ID, request);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            assertThat(result.userId()).isEqualTo(USER_ID);
            assertThat(result.nextStep()).isEqualTo("CAREER_DETAILS");
            verify(userProfileRepository).save(any(UserProfileEntity.class));
        }

        @Test
        @DisplayName("유저가 존재하지 않으면 USER_NOT_FOUND 예외를 던진다")
        void saveCareerInfo_userNotFound() {
            // given
            given(userRepository.findById(USER_ID)).willReturn(Optional.empty());

            OnboardingCareerRequest request = new OnboardingCareerRequest(
                    List.of(CareerStatus.JOB_SEEKER),
                    List.of(JobRole.DEVELOPMENT)
            );

            // when & then
            assertThatThrownBy(() -> userProfileService.saveCareerInfo(USER_ID, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.USER_NOT_FOUND));
        }

        @Test
        @DisplayName("닉네임 미등록 상태이면 NICKNAME_NOT_REGISTERED 예외를 던진다")
        void saveCareerInfo_nicknameNotRegistered() {
            // given
            ReflectionTestUtils.setField(testUser, "registrationStatus", RegistrationStatus.TERMS_AGREED);
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(testUser));

            OnboardingCareerRequest request = new OnboardingCareerRequest(
                    List.of(CareerStatus.JOB_SEEKER),
                    List.of(JobRole.DEVELOPMENT)
            );

            // when & then
            assertThatThrownBy(() -> userProfileService.saveCareerInfo(USER_ID, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.NICKNAME_NOT_REGISTERED));
        }
    }

    // =========================================================================
    // saveBasicInfo
    // =========================================================================
    @Nested
    @DisplayName("saveBasicInfo")
    class SaveBasicInfo {

        @Test
        @DisplayName("정상 케이스 - 기본 정보를 저장하고 OnboardingResponse를 반환한다")
        void saveBasicInfo_success() {
            // given
            ReflectionTestUtils.setField(testUser, "registrationStatus", RegistrationStatus.NICKNAME_REGISTERED);

            OnboardingBasicRequest request = new OnboardingBasicRequest(EducationLevel.BACHELOR, 3);

            OnboardingResponse expectedResponse = OnboardingResponse.builder()
                    .userId(USER_ID)
                    .message("기본 정보가 저장되었습니다.")
                    .nextStep("CAREER_INFO")
                    .build();

            given(userRepository.findById(USER_ID)).willReturn(Optional.of(testUser));
            given(userProfileRepository.findById(USER_ID)).willReturn(Optional.of(testProfile));
            given(userProfileRepository.save(any(UserProfileEntity.class))).willReturn(testProfile);
            given(userMapper.toOnboardingResponse(USER_ID, "기본 정보가 저장되었습니다.", "CAREER_INFO"))
                    .willReturn(expectedResponse);

            // when
            OnboardingResponse result = userProfileService.saveBasicInfo(USER_ID, request);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            assertThat(result.nextStep()).isEqualTo("CAREER_INFO");
            verify(userProfileRepository).save(any(UserProfileEntity.class));
        }

        @Test
        @DisplayName("careerYears가 null이면 compact constructor에서 0으로 기본값 설정된다")
        void saveBasicInfo_careerYearsNullDefaultsToZero() {
            // given
            ReflectionTestUtils.setField(testUser, "registrationStatus", RegistrationStatus.NICKNAME_REGISTERED);

            OnboardingBasicRequest request = new OnboardingBasicRequest(EducationLevel.HIGH_SCHOOL, null);

            OnboardingResponse expectedResponse = OnboardingResponse.builder()
                    .userId(USER_ID)
                    .message("기본 정보가 저장되었습니다.")
                    .nextStep("CAREER_INFO")
                    .build();

            given(userRepository.findById(USER_ID)).willReturn(Optional.of(testUser));
            given(userProfileRepository.findById(USER_ID)).willReturn(Optional.of(testProfile));
            given(userProfileRepository.save(any(UserProfileEntity.class))).willReturn(testProfile);
            given(userMapper.toOnboardingResponse(USER_ID, "기본 정보가 저장되었습니다.", "CAREER_INFO"))
                    .willReturn(expectedResponse);

            // when
            // record compact constructor 에서 null -> 0 변환이 일어남을 검증
            assertThat(request.careerYears()).isEqualTo(0);

            OnboardingResponse result = userProfileService.saveBasicInfo(USER_ID, request);

            // then
            assertThat(result).isEqualTo(expectedResponse);
        }
    }

    // =========================================================================
    // completeOnboarding
    // =========================================================================
    @Nested
    @DisplayName("completeOnboarding")
    class CompleteOnboarding {

        @Test
        @DisplayName("정상 케이스 - 커리어 정보 완료 상태에서 온보딩을 완료한다")
        void completeOnboarding_success() {
            // given
            ReflectionTestUtils.setField(testUser, "registrationStatus", RegistrationStatus.CAREER_INFO_COMPLETED);

            OnboardingResponse expectedResponse = OnboardingResponse.builder()
                    .userId(USER_ID)
                    .message("온보딩이 완료되었습니다.")
                    .nextStep("COMPLETED")
                    .build();

            given(userRepository.findById(USER_ID)).willReturn(Optional.of(testUser));
            given(userMapper.toOnboardingResponse(USER_ID, "온보딩이 완료되었습니다.", "COMPLETED"))
                    .willReturn(expectedResponse);

            // when
            OnboardingResponse result = userProfileService.completeOnboarding(USER_ID);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            assertThat(testUser.getRegistrationStatus()).isEqualTo(RegistrationStatus.ONBOARDING_COMPLETED);
        }

        @Test
        @DisplayName("커리어 정보 미완료 상태이면 CAREER_INFO_NOT_COMPLETED 예외를 던진다")
        void completeOnboarding_careerInfoNotCompleted() {
            // given
            ReflectionTestUtils.setField(testUser, "registrationStatus", RegistrationStatus.NICKNAME_REGISTERED);
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(testUser));

            // when & then
            assertThatThrownBy(() -> userProfileService.completeOnboarding(USER_ID))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CAREER_INFO_NOT_COMPLETED));
        }

        @Test
        @DisplayName("유저가 존재하지 않으면 USER_NOT_FOUND 예외를 던진다")
        void completeOnboarding_userNotFound() {
            // given
            given(userRepository.findById(USER_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userProfileService.completeOnboarding(USER_ID))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.USER_NOT_FOUND));
        }
    }

    // =========================================================================
    // getProfile
    // =========================================================================
    @Nested
    @DisplayName("getProfile")
    class GetProfile {

        @Test
        @DisplayName("정상 케이스 - 프로필 정보를 조회하여 반환한다")
        void getProfile_success() {
            // given
            UserExperiencesEntity exp1 = UserExperiencesEntity.builder()
                    .user(testUser)
                    .description("프로젝트 A 참여")
                    .build();
            UserExperiencesEntity exp2 = UserExperiencesEntity.builder()
                    .user(testUser)
                    .description("인턴십 B 수행")
                    .build();

            UserProfileResponse expectedResponse = new UserProfileResponse(
                    new UserProfileResponse.BasicInfo("testUser", "test@example.com", "google", "자기소개입니다."),
                    new UserProfileResponse.CareerInfo(
                            List.of(CareerStatus.STUDENT),
                            List.of(JobRole.DEVELOPMENT),
                            "3년차",
                            EducationLevel.BACHELOR
                    ),
                    new UserProfileResponse.SelfIntro(
                            List.of("프로젝트 A 참여", "인턴십 B 수행"),
                            List.of("Java", "Spring")
                    )
            );

            given(userRepository.findByIdWithSocialLinks(USER_ID)).willReturn(Optional.of(testUser));
            given(userProfileRepository.findById(USER_ID)).willReturn(Optional.of(testProfile));
            given(userExperiencesRepository.findByUserOrderByCreatedAtAsc(testUser))
                    .willReturn(List.of(exp1, exp2));
            given(userMapper.toUserProfileResponse(eq(testUser), eq(testProfile), anyList()))
                    .willReturn(expectedResponse);

            // when
            UserProfileResponse result = userProfileService.getProfile(USER_ID);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            assertThat(result.basicInfo().nickname()).isEqualTo("testUser");
            assertThat(result.careerInfo().careerYears()).isEqualTo("3년차");
        }

        @Test
        @DisplayName("프로필이 없는 유저 - userProfile이 null로 전달된다")
        void getProfile_noProfile() {
            // given
            UserProfileResponse expectedResponse = new UserProfileResponse(
                    new UserProfileResponse.BasicInfo("testUser", "test@example.com", null, null),
                    new UserProfileResponse.CareerInfo(List.of(), List.of(), "경력 없음", null),
                    new UserProfileResponse.SelfIntro(List.of(), List.of())
            );

            given(userRepository.findByIdWithSocialLinks(USER_ID)).willReturn(Optional.of(testUser));
            given(userProfileRepository.findById(USER_ID)).willReturn(Optional.empty());
            given(userExperiencesRepository.findByUserOrderByCreatedAtAsc(testUser))
                    .willReturn(List.of());
            given(userMapper.toUserProfileResponse(eq(testUser), isNull(), anyList()))
                    .willReturn(expectedResponse);

            // when
            UserProfileResponse result = userProfileService.getProfile(USER_ID);

            // then
            assertThat(result).isEqualTo(expectedResponse);
            verify(userMapper).toUserProfileResponse(eq(testUser), isNull(), eq(List.of()));
        }

        @Test
        @DisplayName("유저가 존재하지 않으면 USER_NOT_FOUND 예외를 던진다")
        void getProfile_userNotFound() {
            // given
            given(userRepository.findByIdWithSocialLinks(USER_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userProfileService.getProfile(USER_ID))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.USER_NOT_FOUND));
        }
    }

    // =========================================================================
    // updateProfile
    // =========================================================================
    @Nested
    @DisplayName("updateProfile")
    class UpdateProfile {

        @Test
        @DisplayName("정상 케이스 - 전체 필드를 업데이트한다")
        void updateProfile_allFields() {
            // given
            ReflectionTestUtils.setField(testUser, "registrationStatus", RegistrationStatus.ONBOARDING_COMPLETED);
            ReflectionTestUtils.setField(testUser, "nicknameChangeCount", 0);

            UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                    new UserProfileUpdateRequest.BasicInfo("newNick", "새로운 자기소개"),
                    new UserProfileUpdateRequest.CareerInfo(
                            List.of(CareerStatus.CAREER_CHANGE_PREP),
                            List.of(JobRole.MARKETING_CONTENT),
                            5,
                            EducationLevel.MASTER
                    ),
                    new UserProfileUpdateRequest.SelfIntro(
                            List.of("경험1", "경험2"),
                            List.of("Kotlin", "React")
                    )
            );

            given(userRepository.findByIdWithSocialLinks(USER_ID)).willReturn(Optional.of(testUser));
            given(userProfileRepository.findById(USER_ID)).willReturn(Optional.of(testProfile));
            given(userRepository.existsByNickname("newNick")).willReturn(false);
            given(userProfileRepository.save(any(UserProfileEntity.class))).willReturn(testProfile);
            given(userMapper.toUserExperienceEntity(eq(testUser), anyString()))
                    .willAnswer(inv -> UserExperiencesEntity.builder()
                            .user(inv.getArgument(0))
                            .description(inv.getArgument(1))
                            .build());

            // when
            UserProfileUpdateResponse result = userProfileService.updateProfile(USER_ID, request);

            // then
            assertThat(result.message()).isEqualTo("프로필 정보가 성공적으로 저장되었습니다.");
            verify(userProfileRepository).save(any(UserProfileEntity.class));
            verify(userExperiencesRepository).deleteByUser(testUser);
            verify(userExperiencesRepository, times(2)).save(any(UserExperiencesEntity.class));
        }

        @Test
        @DisplayName("null 필드는 무시한다 - basicInfo, careerInfo, selfIntro 모두 null")
        void updateProfile_nullFieldsIgnored() {
            // given
            UserProfileUpdateRequest request = new UserProfileUpdateRequest(null, null, null);

            given(userRepository.findByIdWithSocialLinks(USER_ID)).willReturn(Optional.of(testUser));
            given(userProfileRepository.findById(USER_ID)).willReturn(Optional.of(testProfile));
            given(userProfileRepository.save(any(UserProfileEntity.class))).willReturn(testProfile);

            // when
            UserProfileUpdateResponse result = userProfileService.updateProfile(USER_ID, request);

            // then
            assertThat(result.message()).isEqualTo("프로필 정보가 성공적으로 저장되었습니다.");
            verify(userExperiencesRepository, never()).deleteByUser(any());
            verify(userExperiencesRepository, never()).save(any());
        }

        @Test
        @DisplayName("careerYears가 null이면 기존값을 유지한다")
        void updateProfile_careerYearsNullKeepsExisting() {
            // given
            UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                    null,
                    new UserProfileUpdateRequest.CareerInfo(null, null, null, null),
                    null
            );

            given(userRepository.findByIdWithSocialLinks(USER_ID)).willReturn(Optional.of(testUser));
            given(userProfileRepository.findById(USER_ID)).willReturn(Optional.of(testProfile));
            given(userProfileRepository.save(any(UserProfileEntity.class))).willReturn(testProfile);

            // when
            userProfileService.updateProfile(USER_ID, request);

            // then
            // testProfile의 experienceYears가 기존값(3)을 유지하는지 확인
            assertThat(testProfile.getExperienceYears()).isEqualTo(3);
            assertThat(testProfile.getCurrentStatus()).isEqualTo(List.of(CareerStatus.STUDENT));
            assertThat(testProfile.getTargetJob()).isEqualTo(List.of(JobRole.DEVELOPMENT));
            assertThat(testProfile.getEducation()).isEqualTo(EducationLevel.BACHELOR);
        }

        @Test
        @DisplayName("유저가 존재하지 않으면 USER_NOT_FOUND 예외를 던진다")
        void updateProfile_userNotFound() {
            // given
            given(userRepository.findByIdWithSocialLinks(USER_ID)).willReturn(Optional.empty());

            UserProfileUpdateRequest request = new UserProfileUpdateRequest(null, null, null);

            // when & then
            assertThatThrownBy(() -> userProfileService.updateProfile(USER_ID, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.USER_NOT_FOUND));
        }
    }

    // =========================================================================
    // registerNickname
    // =========================================================================
    @Nested
    @DisplayName("registerNickname")
    class RegisterNickname {

        @Test
        @DisplayName("정상 케이스 - 닉네임을 등록하고 성공 메시지를 반환한다")
        void registerNickname_success() {
            // given
            ReflectionTestUtils.setField(testUser, "nicknameChangeCount", 0);
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(testUser));
            given(userRepository.existsByNickname("validNick")).willReturn(false);

            // when
            NicknameRegisterResponse result = userProfileService.registerNickname(USER_ID, "validNick");

            // then
            assertThat(result.message()).isEqualTo("닉네임이 성공적으로 등록되었습니다.");
            assertThat(testUser.getNickname()).isEqualTo("validNick");
            assertThat(testUser.getRegistrationStatus()).isEqualTo(RegistrationStatus.NICKNAME_REGISTERED);
        }

        @Test
        @DisplayName("중복 닉네임 - existsByNickname이 true를 반환하면 NICKNAME_ALREADY_USED 예외")
        void registerNickname_duplicateNickname() {
            // given
            ReflectionTestUtils.setField(testUser, "nicknameChangeCount", 0);
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(testUser));
            given(userRepository.existsByNickname("duplicated")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userProfileService.registerNickname(USER_ID, "duplicated"))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.NICKNAME_ALREADY_USED));
        }

        @Test
        @DisplayName("포맷 위반 - 특수문자 포함 닉네임은 INVALID_NICKNAME_FORMAT 예외")
        void registerNickname_invalidFormat() {
            // given
            ReflectionTestUtils.setField(testUser, "nicknameChangeCount", 0);
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(testUser));

            // when & then
            assertThatThrownBy(() -> userProfileService.registerNickname(USER_ID, "invalid@nick!"))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_NICKNAME_FORMAT));
        }

        @Test
        @DisplayName("포맷 위반 - 13자 초과 닉네임은 INVALID_NICKNAME_FORMAT 예외")
        void registerNickname_tooLongNickname() {
            // given
            ReflectionTestUtils.setField(testUser, "nicknameChangeCount", 0);
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(testUser));

            // when & then
            assertThatThrownBy(() -> userProfileService.registerNickname(USER_ID, "abcdefghijklm"))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_NICKNAME_FORMAT));
        }

        @Test
        @DisplayName("쿨다운 - 무료 변경 횟수 초과 후 24시간 이내 변경 시 NICKNAME_CHANGE_TOO_FREQUENT 예외")
        void registerNickname_cooldown() {
            // given
            ReflectionTestUtils.setField(testUser, "nicknameChangeCount", 2);
            ReflectionTestUtils.setField(testUser, "lastNicknameChangedAt", LocalDateTime.now().minusHours(1));
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(testUser));
            given(userRepository.existsByNickname("newNick")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userProfileService.registerNickname(USER_ID, "newNick"))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.NICKNAME_CHANGE_TOO_FREQUENT));
        }

        @Test
        @DisplayName("쿨다운 경과 - 24시간 초과 후에는 닉네임 변경이 가능하다")
        void registerNickname_cooldownExpired() {
            // given
            ReflectionTestUtils.setField(testUser, "nicknameChangeCount", 2);
            ReflectionTestUtils.setField(testUser, "lastNicknameChangedAt", LocalDateTime.now().minusHours(25));
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(testUser));
            given(userRepository.existsByNickname("newNick")).willReturn(false);

            // when
            NicknameRegisterResponse result = userProfileService.registerNickname(USER_ID, "newNick");

            // then
            assertThat(result.message()).isEqualTo("닉네임이 성공적으로 등록되었습니다.");
            assertThat(testUser.getNickname()).isEqualTo("newNick");
        }

        @Test
        @DisplayName("DB 중복 예외 - DataIntegrityViolationException 발생 시 NICKNAME_ALREADY_USED 변환")
        void registerNickname_dataIntegrityViolation() {
            // given: race condition - existsByNickname 통과 후 DB unique constraint 위반
            ReflectionTestUtils.setField(testUser, "nicknameChangeCount", 0);
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(testUser));
            given(userRepository.existsByNickname("raceNick")).willReturn(false);

            // existsByNickname 이후 changeNicknameManually 호출 시
            // 실제 DB flush 시점에서 DataIntegrityViolationException 발생을 시뮬레이션
            // Spy를 사용하여 changeNicknameManually에서 예외 발생
            UserEntity spyUser = spy(testUser);
            ReflectionTestUtils.setField(spyUser, "nicknameChangeCount", 0);
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(spyUser));
            doThrow(new DataIntegrityViolationException("Duplicate entry"))
                    .when(spyUser).changeNicknameManually(eq("raceNick"), any(LocalDateTime.class));

            // when & then
            assertThatThrownBy(() -> userProfileService.registerNickname(USER_ID, "raceNick"))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.NICKNAME_ALREADY_USED));
        }

        @Test
        @DisplayName("유저가 존재하지 않으면 USER_NOT_FOUND 예외를 던진다")
        void registerNickname_userNotFound() {
            // given
            given(userRepository.findById(USER_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userProfileService.registerNickname(USER_ID, "anyNick"))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.USER_NOT_FOUND));
        }

        @Test
        @DisplayName("빈 문자열 닉네임은 INVALID_NICKNAME_FORMAT 예외를 던진다")
        void registerNickname_emptyNickname() {
            // given
            ReflectionTestUtils.setField(testUser, "nicknameChangeCount", 0);
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(testUser));

            // when & then
            assertThatThrownBy(() -> userProfileService.registerNickname(USER_ID, "  "))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_NICKNAME_FORMAT));
        }
    }
}
