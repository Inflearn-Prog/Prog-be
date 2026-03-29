package com.progbe.domain.terms.service;

import com.progbe.domain.terms.dto.*;
import com.progbe.domain.terms.entity.TermsEntity;
import com.progbe.domain.terms.entity.UserTermsAgreementEntity;
import com.progbe.domain.terms.mapper.TermsMapper;
import com.progbe.domain.terms.repository.TermsRepository;
import com.progbe.domain.terms.repository.UserTermsAgreementRepository;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.domain.user.repository.UserRepository;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermsService {

    private final TermsRepository termsRepository;
    private final UserRepository userRepository;
    private final UserTermsAgreementRepository userTermsAgreementRepository;
    private final TermsMapper termsMapper;

    public TermsListResponse getAllTerms() {
        List<TermsResponseDto> terms = termsRepository.findAllByOrderByRequiredDescIdAsc().stream()
                .map(termsMapper::toTermsResponseDto)
                .toList();

        return new TermsListResponse(terms);
    }

    @Transactional
    public TermsAgreementResponse processAgreement(Long userId, TermsAgreementRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Long> agreedTermIds = request.agreedTermIds();
        
        // 1. 모든 약관 조회 (캐싱 고려 가능)
        List<TermsEntity> allTerms = termsRepository.findAll();
        Map<Long, TermsEntity> termsMap = allTerms.stream()
                .collect(Collectors.toMap(TermsEntity::getId, Function.identity()));

        // 2. 존재하지 않는 약관 ID 검증
        for (Long termId : agreedTermIds) {
            if (!termsMap.containsKey(termId)) {
                throw new CustomException(ErrorCode.TERMS_NOT_FOUND);
            }
        }

        // 2-1. 필수 약관 전체 동의 검증
        boolean allRequiredAgreed = allTerms.stream()
                .filter(TermsEntity::getRequired)
                .allMatch(t -> agreedTermIds.contains(t.getId()));
        if (!allRequiredAgreed) {
            throw new CustomException(ErrorCode.REQUIRED_TERMS_NOT_AGREED);
        }

        // 3. 약관 동의 내역 저장 (중복 체크 포함)
        for (Long termId : agreedTermIds) {
            TermsEntity term = termsMap.get(termId);
            
            // 기존 동의 내역 확인
            userTermsAgreementRepository.findByUserIdAndTermsId(userId, termId)
                    .ifPresentOrElse(
                            existingAgreement -> {
                                // 이미 동의한 약관인 경우
                                if (existingAgreement.getIsAgreed() && existingAgreement.getWithdrawnAt() == null) {
                                    throw new CustomException(ErrorCode.TERMS_ALREADY_AGREED);
                                }
                                // 철회했던 약관을 재동의하는 경우
                                existingAgreement.reAgree();
                            },
                            () -> {
                                // 새로운 동의인 경우
                                UserTermsAgreementEntity agreement = UserTermsAgreementEntity.builder()
                                        .user(user)
                                        .terms(term)
                                        .isAgreed(true)
                                        .version(term.getUpdatedAt() != null ? term.getUpdatedAt() : LocalDateTime.now())
                                        .build();
                                
                                userTermsAgreementRepository.save(agreement);
                            }
                    );
        }

        // 4. 약관 동의 완료 시 회원가입 상태 업데이트
        user.updateRegistrationStatus(com.progbe.domain.user.type.RegistrationStatus.TERMS_AGREED);

        return termsMapper.toTermsAgreementResponse(userId, true);
    }

    @Transactional
    public TermsWithdrawalResponse withdrawTermsAgreement(Long userId, TermsWithdrawalRequest request) {
        if (!userRepository.existsById(userId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        List<Long> termIds = request.termIds();
        
        if (termIds == null || termIds.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 필수 약관 철회 차단
        List<TermsEntity> requiredTerms = termsRepository.findAll().stream()
                .filter(TermsEntity::getRequired)
                .toList();
        for (TermsEntity required : requiredTerms) {
            if (termIds.contains(required.getId())) {
                throw new CustomException(ErrorCode.REQUIRED_TERMS_CANNOT_WITHDRAW);
            }
        }

        List<Long> withdrawnTermIds = new ArrayList<>();

        for (Long termId : termIds) {
            UserTermsAgreementEntity agreement = userTermsAgreementRepository
                    .findByUserIdAndTermsIdForUpdate(userId, termId)
                    .orElseThrow(() -> new CustomException(ErrorCode.TERMS_AGREEMENT_NOT_FOUND));

            if (!agreement.getIsAgreed() || agreement.getWithdrawnAt() != null) {
                throw new CustomException(ErrorCode.TERMS_ALREADY_WITHDRAWN);
            }

            agreement.withdraw();
            withdrawnTermIds.add(termId);
        }

        return termsMapper.toTermsWithdrawalResponse(userId, withdrawnTermIds);
    }

    public UserAgreedTermsResponse getUserAgreedTerms(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        List<UserTermsAgreementEntity> agreements = userTermsAgreementRepository
                .findAllByUserIdAndIsAgreedTrueWithTerms(userId);

        List<UserAgreedTermDto> agreedTerms = agreements.stream()
                .map(termsMapper::toUserAgreedTermDto)
                .toList();

        return termsMapper.toUserAgreedTermsResponse(userId, agreedTerms);
    }
}
