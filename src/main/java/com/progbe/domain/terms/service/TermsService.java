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

        // 2. 약관 동의 내역 저장
        for (Long termId : agreedTermIds) {
            TermsEntity term = termsMap.get(termId);
            if (term != null) {
                UserTermsAgreementEntity agreement = UserTermsAgreementEntity.builder()
                        .user(user)
                        .terms(term)
                        .isAgreed(true)
                        .version(term.getUpdatedAt() != null ? term.getUpdatedAt() : LocalDateTime.now())
                        .build();
                
                userTermsAgreementRepository.save(agreement);
            }
        }

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
