package com.progbe.domain.terms.service;

import com.progbe.domain.terms.dto.*;
import com.progbe.domain.terms.entity.TermsEntity;
import com.progbe.domain.terms.entity.UserTermsAgreementEntity;
import com.progbe.domain.terms.mapper.TermsMapper;
import com.progbe.domain.terms.repository.TermsRepository;
import com.progbe.domain.terms.repository.UserTermsAgreementRepository;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.domain.user.repository.UserRepository;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TermsServiceTest {

    @Mock
    private TermsRepository termsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserTermsAgreementRepository userTermsAgreementRepository;

    @Mock
    private TermsMapper termsMapper;

    @InjectMocks
    private TermsService termsService;

    private UserEntity testUser;
    private TermsEntity requiredTerm1;
    private TermsEntity requiredTerm2;
    private TermsEntity optionalTerm;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .nickname("testUser")
                .email("test@test.com")
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();
        setIdViaReflection(testUser, UserEntity.class, 1L);

        requiredTerm1 = createTermsEntity(1L, "서비스 이용약관", true);
        requiredTerm2 = createTermsEntity(2L, "개인정보 처리방침", true);
        optionalTerm = createTermsEntity(3L, "마케팅 수신 동의", false);
    }

    // ========================================================================
    // getAllTerms
    // ========================================================================
    @Nested
    @DisplayName("getAllTerms - 약관 목록 조회")
    class GetAllTermsTest {

        @Test
        @DisplayName("정상적으로 모든 약관을 조회한다")
        void getAllTerms_success() {
            // given
            List<TermsEntity> allTerms = List.of(requiredTerm1, requiredTerm2, optionalTerm);
            given(termsRepository.findAllByOrderByRequiredDescIdAsc()).willReturn(allTerms);

            TermsResponseDto dto1 = TermsResponseDto.builder().termId(1L).title("서비스 이용약관").isRequired(true).build();
            TermsResponseDto dto2 = TermsResponseDto.builder().termId(2L).title("개인정보 처리방침").isRequired(true).build();
            TermsResponseDto dto3 = TermsResponseDto.builder().termId(3L).title("마케팅 수신 동의").isRequired(false).build();

            given(termsMapper.toTermsResponseDto(requiredTerm1)).willReturn(dto1);
            given(termsMapper.toTermsResponseDto(requiredTerm2)).willReturn(dto2);
            given(termsMapper.toTermsResponseDto(optionalTerm)).willReturn(dto3);

            // when
            TermsListResponse result = termsService.getAllTerms();

            // then
            assertThat(result.terms()).hasSize(3);
            assertThat(result.terms().get(0).isRequired()).isTrue();
            assertThat(result.terms().get(2).isRequired()).isFalse();
            verify(termsRepository).findAllByOrderByRequiredDescIdAsc();
        }

        @Test
        @DisplayName("약관이 없으면 빈 리스트를 반환한다")
        void getAllTerms_empty() {
            // given
            given(termsRepository.findAllByOrderByRequiredDescIdAsc()).willReturn(Collections.emptyList());

            // when
            TermsListResponse result = termsService.getAllTerms();

            // then
            assertThat(result.terms()).isEmpty();
        }
    }

    // ========================================================================
    // processAgreement
    // ========================================================================
    @Nested
    @DisplayName("processAgreement - 약관 동의 처리")
    class ProcessAgreementTest {

        private final Long userId = 1L;

        @Test
        @DisplayName("신규 사용자가 필수+선택 약관 모두 동의하면 성공한다")
        void processAgreement_allTerms_success() {
            // given
            List<Long> agreedTermIds = List.of(1L, 2L, 3L);
            TermsAgreementRequest request = new TermsAgreementRequest(agreedTermIds);

            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(termsRepository.findAll()).willReturn(List.of(requiredTerm1, requiredTerm2, optionalTerm));
            given(userTermsAgreementRepository.findAllByUserIdAndIsAgreedTrueWithTerms(userId))
                    .willReturn(Collections.emptyList());
            // 모두 신규 동의 -> findByUserIdAndTermsId는 empty
            given(userTermsAgreementRepository.findByUserIdAndTermsId(eq(userId), anyLong()))
                    .willReturn(Optional.empty());

            TermsAgreementResponse expectedResponse = TermsAgreementResponse.builder()
                    .userId(userId).isRegistrationComplete(true)
                    .message("약관 동의가 성공적으로 처리되었습니다.").build();
            given(termsMapper.toTermsAgreementResponse(userId, true)).willReturn(expectedResponse);

            // when
            TermsAgreementResponse result = termsService.processAgreement(userId, request);

            // then
            assertThat(result.userId()).isEqualTo(userId);
            assertThat(result.isRegistrationComplete()).isTrue();
            verify(userTermsAgreementRepository, times(3)).save(any(UserTermsAgreementEntity.class));
        }

        @Test
        @DisplayName("필수 약관만 동의해도 성공한다")
        void processAgreement_requiredOnly_success() {
            // given
            List<Long> agreedTermIds = List.of(1L, 2L);
            TermsAgreementRequest request = new TermsAgreementRequest(agreedTermIds);

            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(termsRepository.findAll()).willReturn(List.of(requiredTerm1, requiredTerm2, optionalTerm));
            given(userTermsAgreementRepository.findAllByUserIdAndIsAgreedTrueWithTerms(userId))
                    .willReturn(Collections.emptyList());
            given(userTermsAgreementRepository.findByUserIdAndTermsId(eq(userId), anyLong()))
                    .willReturn(Optional.empty());

            TermsAgreementResponse expectedResponse = TermsAgreementResponse.builder()
                    .userId(userId).isRegistrationComplete(true).build();
            given(termsMapper.toTermsAgreementResponse(userId, true)).willReturn(expectedResponse);

            // when
            TermsAgreementResponse result = termsService.processAgreement(userId, request);

            // then
            assertThat(result).isNotNull();
            verify(userTermsAgreementRepository, times(2)).save(any(UserTermsAgreementEntity.class));
        }

        @Test
        @DisplayName("필수 약관이 누락되면 REQUIRED_TERMS_NOT_AGREED 에러를 던진다")
        void processAgreement_missingRequired_throwsError() {
            // given - 필수 약관 2번 누락
            List<Long> agreedTermIds = List.of(1L, 3L);
            TermsAgreementRequest request = new TermsAgreementRequest(agreedTermIds);

            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(termsRepository.findAll()).willReturn(List.of(requiredTerm1, requiredTerm2, optionalTerm));
            given(userTermsAgreementRepository.findAllByUserIdAndIsAgreedTrueWithTerms(userId))
                    .willReturn(Collections.emptyList());

            // when & then
            assertThatThrownBy(() -> termsService.processAgreement(userId, request))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.REQUIRED_TERMS_NOT_AGREED);
        }

        @Test
        @DisplayName("[Catch-22 해소] 이미 동의한 필수 약관을 다시 보내도 스킵하고 성공한다")
        void processAgreement_alreadyAgreedTermsIncluded_skipsAndSucceeds() {
            // given
            // 사용자가 이미 필수약관 1번에 동의한 상태
            UserTermsAgreementEntity existingAgreement = UserTermsAgreementEntity.builder()
                    .user(testUser)
                    .terms(requiredTerm1)
                    .isAgreed(true)
                    .version(LocalDateTime.now())
                    .build();

            // 클라이언트가 모든 약관 ID를 다시 보냄 (이미 동의한 1번 포함)
            List<Long> agreedTermIds = List.of(1L, 2L, 3L);
            TermsAgreementRequest request = new TermsAgreementRequest(agreedTermIds);

            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(termsRepository.findAll()).willReturn(List.of(requiredTerm1, requiredTerm2, optionalTerm));
            given(userTermsAgreementRepository.findAllByUserIdAndIsAgreedTrueWithTerms(userId))
                    .willReturn(List.of(existingAgreement));

            // 1번 약관은 이미 존재 -> isAgreed=true, withdrawnAt=null -> 스킵
            given(userTermsAgreementRepository.findByUserIdAndTermsId(userId, 1L))
                    .willReturn(Optional.of(existingAgreement));
            // 2번, 3번은 신규
            given(userTermsAgreementRepository.findByUserIdAndTermsId(userId, 2L))
                    .willReturn(Optional.empty());
            given(userTermsAgreementRepository.findByUserIdAndTermsId(userId, 3L))
                    .willReturn(Optional.empty());

            TermsAgreementResponse expectedResponse = TermsAgreementResponse.builder()
                    .userId(userId).isRegistrationComplete(true).build();
            given(termsMapper.toTermsAgreementResponse(userId, true)).willReturn(expectedResponse);

            // when
            TermsAgreementResponse result = termsService.processAgreement(userId, request);

            // then - 성공하고, 이미 동의한 약관에 대해서는 save가 호출되지 않아야 함
            assertThat(result).isNotNull();
            // save는 2번, 3번 약관에 대해서만 (2회)
            verify(userTermsAgreementRepository, times(2)).save(any(UserTermsAgreementEntity.class));
        }

        @Test
        @DisplayName("[Catch-22 해소] 이미 동의한 필수 약관이 있으면, 나머지 필수 약관만 보내도 통과한다")
        void processAgreement_partialNewAgreement_withExistingRequired_succeeds() {
            // given
            // 사용자가 이미 필수약관 1번에 동의한 상태
            UserTermsAgreementEntity existingAgreement = UserTermsAgreementEntity.builder()
                    .user(testUser)
                    .terms(requiredTerm1)
                    .isAgreed(true)
                    .version(LocalDateTime.now())
                    .build();

            // 클라이언트가 나머지 필수약관 2번만 보냄 (이미 동의한 1번은 보내지 않음)
            List<Long> agreedTermIds = List.of(2L);
            TermsAgreementRequest request = new TermsAgreementRequest(agreedTermIds);

            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(termsRepository.findAll()).willReturn(List.of(requiredTerm1, requiredTerm2, optionalTerm));
            given(userTermsAgreementRepository.findAllByUserIdAndIsAgreedTrueWithTerms(userId))
                    .willReturn(List.of(existingAgreement));

            given(userTermsAgreementRepository.findByUserIdAndTermsId(userId, 2L))
                    .willReturn(Optional.empty());

            TermsAgreementResponse expectedResponse = TermsAgreementResponse.builder()
                    .userId(userId).isRegistrationComplete(true).build();
            given(termsMapper.toTermsAgreementResponse(userId, true)).willReturn(expectedResponse);

            // when
            TermsAgreementResponse result = termsService.processAgreement(userId, request);

            // then - 기존 동의 + 새 동의 합산하여 필수 약관 충족
            assertThat(result).isNotNull();
            verify(userTermsAgreementRepository, times(1)).save(any(UserTermsAgreementEntity.class));
        }

        @Test
        @DisplayName("존재하지 않는 약관 ID가 포함되면 TERMS_NOT_FOUND 에러를 던진다")
        void processAgreement_invalidTermId_throwsError() {
            // given
            List<Long> agreedTermIds = List.of(1L, 2L, 999L);
            TermsAgreementRequest request = new TermsAgreementRequest(agreedTermIds);

            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(termsRepository.findAll()).willReturn(List.of(requiredTerm1, requiredTerm2, optionalTerm));

            // when & then
            assertThatThrownBy(() -> termsService.processAgreement(userId, request))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.TERMS_NOT_FOUND);
        }

        @Test
        @DisplayName("존재하지 않는 사용자이면 USER_NOT_FOUND 에러를 던진다")
        void processAgreement_userNotFound_throwsError() {
            // given
            TermsAgreementRequest request = new TermsAgreementRequest(List.of(1L, 2L));
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> termsService.processAgreement(userId, request))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("철회했던 약관을 재동의하면 reAgree가 호출된다")
        void processAgreement_reAgreeWithdrawnTerm_success() {
            // given
            List<Long> agreedTermIds = List.of(1L, 2L, 3L);
            TermsAgreementRequest request = new TermsAgreementRequest(agreedTermIds);

            // 3번 선택 약관을 이전에 동의 후 철회한 상태
            UserTermsAgreementEntity withdrawnAgreement = mock(UserTermsAgreementEntity.class);
            given(withdrawnAgreement.getIsAgreed()).willReturn(false);

            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(termsRepository.findAll()).willReturn(List.of(requiredTerm1, requiredTerm2, optionalTerm));
            given(userTermsAgreementRepository.findAllByUserIdAndIsAgreedTrueWithTerms(userId))
                    .willReturn(Collections.emptyList());

            given(userTermsAgreementRepository.findByUserIdAndTermsId(userId, 1L))
                    .willReturn(Optional.empty());
            given(userTermsAgreementRepository.findByUserIdAndTermsId(userId, 2L))
                    .willReturn(Optional.empty());
            given(userTermsAgreementRepository.findByUserIdAndTermsId(userId, 3L))
                    .willReturn(Optional.of(withdrawnAgreement));

            TermsAgreementResponse expectedResponse = TermsAgreementResponse.builder()
                    .userId(userId).isRegistrationComplete(true).build();
            given(termsMapper.toTermsAgreementResponse(userId, true)).willReturn(expectedResponse);

            // when
            termsService.processAgreement(userId, request);

            // then - 철회했던 약관은 reAgree(), 나머지 2개는 save()
            verify(withdrawnAgreement).reAgree();
            verify(userTermsAgreementRepository, times(2)).save(any(UserTermsAgreementEntity.class));
        }
    }

    // ========================================================================
    // withdrawTermsAgreement
    // ========================================================================
    @Nested
    @DisplayName("withdrawTermsAgreement - 약관 동의 철회")
    class WithdrawTermsAgreementTest {

        private final Long userId = 1L;

        @Test
        @DisplayName("선택 약관 철회에 성공한다")
        void withdrawTermsAgreement_optional_success() {
            // given
            TermsWithdrawalRequest request = new TermsWithdrawalRequest(List.of(3L));

            UserTermsAgreementEntity agreement = mock(UserTermsAgreementEntity.class);
            given(agreement.getIsAgreed()).willReturn(true);
            given(agreement.getWithdrawnAt()).willReturn(null);

            given(userRepository.existsById(userId)).willReturn(true);
            given(termsRepository.findAll()).willReturn(List.of(requiredTerm1, requiredTerm2, optionalTerm));
            given(userTermsAgreementRepository.findByUserIdAndTermsIdForUpdate(userId, 3L))
                    .willReturn(Optional.of(agreement));

            TermsWithdrawalResponse expectedResponse = TermsWithdrawalResponse.builder()
                    .userId(userId).withdrawnTermIds(List.of(3L)).build();
            given(termsMapper.toTermsWithdrawalResponse(userId, List.of(3L))).willReturn(expectedResponse);

            // when
            TermsWithdrawalResponse result = termsService.withdrawTermsAgreement(userId, request);

            // then
            assertThat(result.withdrawnTermIds()).containsExactly(3L);
            verify(agreement).withdraw();
        }

        @Test
        @DisplayName("필수 약관을 철회하려 하면 REQUIRED_TERMS_CANNOT_WITHDRAW 에러를 던진다")
        void withdrawTermsAgreement_requiredTerm_throwsError() {
            // given
            TermsWithdrawalRequest request = new TermsWithdrawalRequest(List.of(1L));

            given(userRepository.existsById(userId)).willReturn(true);
            given(termsRepository.findAll()).willReturn(List.of(requiredTerm1, requiredTerm2, optionalTerm));

            // when & then
            assertThatThrownBy(() -> termsService.withdrawTermsAgreement(userId, request))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.REQUIRED_TERMS_CANNOT_WITHDRAW);
        }

        @Test
        @DisplayName("존재하지 않는 사용자이면 USER_NOT_FOUND 에러를 던진다")
        void withdrawTermsAgreement_userNotFound_throwsError() {
            // given
            TermsWithdrawalRequest request = new TermsWithdrawalRequest(List.of(3L));
            given(userRepository.existsById(userId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> termsService.withdrawTermsAgreement(userId, request))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("동의 내역이 없는 약관을 철회하면 TERMS_AGREEMENT_NOT_FOUND 에러를 던진다")
        void withdrawTermsAgreement_noAgreement_throwsError() {
            // given
            TermsWithdrawalRequest request = new TermsWithdrawalRequest(List.of(3L));

            given(userRepository.existsById(userId)).willReturn(true);
            given(termsRepository.findAll()).willReturn(List.of(requiredTerm1, requiredTerm2, optionalTerm));
            given(userTermsAgreementRepository.findByUserIdAndTermsIdForUpdate(userId, 3L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> termsService.withdrawTermsAgreement(userId, request))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.TERMS_AGREEMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("이미 철회된 약관을 다시 철회하면 TERMS_ALREADY_WITHDRAWN 에러를 던진다")
        void withdrawTermsAgreement_alreadyWithdrawn_throwsError() {
            // given
            TermsWithdrawalRequest request = new TermsWithdrawalRequest(List.of(3L));

            UserTermsAgreementEntity agreement = mock(UserTermsAgreementEntity.class);
            given(agreement.getIsAgreed()).willReturn(false);

            given(userRepository.existsById(userId)).willReturn(true);
            given(termsRepository.findAll()).willReturn(List.of(requiredTerm1, requiredTerm2, optionalTerm));
            given(userTermsAgreementRepository.findByUserIdAndTermsIdForUpdate(userId, 3L))
                    .willReturn(Optional.of(agreement));

            // when & then
            assertThatThrownBy(() -> termsService.withdrawTermsAgreement(userId, request))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.TERMS_ALREADY_WITHDRAWN);
        }

        @Test
        @DisplayName("termIds가 null이면 INVALID_INPUT_VALUE 에러를 던진다")
        void withdrawTermsAgreement_nullTermIds_throwsError() {
            // given
            TermsWithdrawalRequest request = new TermsWithdrawalRequest(null);
            given(userRepository.existsById(userId)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> termsService.withdrawTermsAgreement(userId, request))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
        }

        @Test
        @DisplayName("termIds가 빈 리스트이면 INVALID_INPUT_VALUE 에러를 던진다")
        void withdrawTermsAgreement_emptyTermIds_throwsError() {
            // given
            TermsWithdrawalRequest request = new TermsWithdrawalRequest(Collections.emptyList());
            given(userRepository.existsById(userId)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> termsService.withdrawTermsAgreement(userId, request))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    // ========================================================================
    // getUserAgreedTerms
    // ========================================================================
    @Nested
    @DisplayName("getUserAgreedTerms - 사용자 동의 약관 조회")
    class GetUserAgreedTermsTest {

        private final Long userId = 1L;

        @Test
        @DisplayName("사용자가 동의한 약관 목록을 정상 조회한다")
        void getUserAgreedTerms_success() {
            // given
            UserTermsAgreementEntity agreement = UserTermsAgreementEntity.builder()
                    .user(testUser)
                    .terms(requiredTerm1)
                    .isAgreed(true)
                    .version(LocalDateTime.now())
                    .build();

            UserAgreedTermDto dto = UserAgreedTermDto.builder()
                    .termId(1L).title("서비스 이용약관").isRequired(true)
                    .agreedAt(LocalDateTime.now()).build();

            UserAgreedTermsResponse expectedResponse = UserAgreedTermsResponse.builder()
                    .userId(userId).agreedTerms(List.of(dto)).build();

            given(userRepository.existsById(userId)).willReturn(true);
            given(userTermsAgreementRepository.findAllByUserIdAndIsAgreedTrueWithTerms(userId))
                    .willReturn(List.of(agreement));
            given(termsMapper.toUserAgreedTermDto(agreement)).willReturn(dto);
            given(termsMapper.toUserAgreedTermsResponse(eq(userId), anyList())).willReturn(expectedResponse);

            // when
            UserAgreedTermsResponse result = termsService.getUserAgreedTerms(userId);

            // then
            assertThat(result.userId()).isEqualTo(userId);
            assertThat(result.agreedTerms()).hasSize(1);
            assertThat(result.agreedTerms().get(0).termId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("동의한 약관이 없으면 빈 리스트를 반환한다")
        void getUserAgreedTerms_empty() {
            // given
            UserAgreedTermsResponse expectedResponse = UserAgreedTermsResponse.builder()
                    .userId(userId).agreedTerms(Collections.emptyList()).build();

            given(userRepository.existsById(userId)).willReturn(true);
            given(userTermsAgreementRepository.findAllByUserIdAndIsAgreedTrueWithTerms(userId))
                    .willReturn(Collections.emptyList());
            given(termsMapper.toUserAgreedTermsResponse(eq(userId), anyList())).willReturn(expectedResponse);

            // when
            UserAgreedTermsResponse result = termsService.getUserAgreedTerms(userId);

            // then
            assertThat(result.agreedTerms()).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 사용자이면 USER_NOT_FOUND 에러를 던진다")
        void getUserAgreedTerms_userNotFound_throwsError() {
            // given
            given(userRepository.existsById(userId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> termsService.getUserAgreedTerms(userId))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================
    private TermsEntity createTermsEntity(Long id, String title, boolean required) {
        TermsEntity entity = TermsEntity.builder()
                .title(title)
                .required(required)
                .link("https://example.com/terms/" + id)
                .build();
        setIdViaReflection(entity, TermsEntity.class, id);
        return entity;
    }

    private <T> void setIdViaReflection(T entity, Class<T> clazz, Long id) {
        try {
            java.lang.reflect.Field idField = clazz.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id via reflection", e);
        }
    }
}
