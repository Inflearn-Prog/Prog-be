package com.progbe.domain.terms.mapper;

import com.progbe.domain.terms.dto.*;
import com.progbe.domain.terms.entity.TermsEntity;
import com.progbe.domain.terms.entity.UserTermsAgreementEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TermsMapper {

    public TermsResponseDto toTermsResponseDto(TermsEntity entity) {
        return TermsResponseDto.builder()
                .termId(entity.getId())
                .title(entity.getTitle())
                .isRequired(entity.getRequired())
                .hasDetails(entity.getLink() != null && !entity.getLink().isBlank())
                .link(entity.getLink())
                .build();
    }

    public TermsAgreementResponse toTermsAgreementResponse(Long userId, boolean isRegistrationComplete) {
        return TermsAgreementResponse.builder()
                .userId(userId)
                .isRegistrationComplete(isRegistrationComplete)
                .message("약관 동의가 성공적으로 처리되었습니다.")
                .build();
    }

    public TermsWithdrawalResponse toTermsWithdrawalResponse(Long userId, List<Long> withdrawnTermIds) {
        return TermsWithdrawalResponse.builder()
                .userId(userId)
                .withdrawnTermIds(withdrawnTermIds)
                .message("선택하신 약관의 동의 철회가 성공적으로 처리되었습니다.")
                .build();
    }

    public UserAgreedTermDto toUserAgreedTermDto(UserTermsAgreementEntity agreement) {
        return UserAgreedTermDto.builder()
                .termId(agreement.getTerms().getId())
                .title(agreement.getTerms().getTitle())
                .isRequired(agreement.getTerms().getRequired())
                .agreedAt(agreement.getAgreedAt())
                .build();
    }

    public UserAgreedTermsResponse toUserAgreedTermsResponse(Long userId, List<UserAgreedTermDto> agreedTerms) {
        return UserAgreedTermsResponse.builder()
                .userId(userId)
                .agreedTerms(agreedTerms)
                .build();
    }
}
