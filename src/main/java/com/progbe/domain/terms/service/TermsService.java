package com.progbe.domain.terms.service;

import com.progbe.domain.terms.dto.TermsAgreementRequest;
import com.progbe.domain.terms.dto.TermsAgreementResponse;
import com.progbe.domain.terms.dto.TermsResponseDto;
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

    public List<TermsResponseDto> getAllTerms() {
        return termsRepository.findAllByOrderByRequiredDescIdAsc().stream()
                .map(termsMapper::toTermsResponseDto)
                .collect(Collectors.toList());
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
}
