package com.progbe.domain.terms.mapper;

import com.progbe.domain.terms.dto.TermsAgreementResponse;
import com.progbe.domain.terms.dto.TermsResponseDto;
import com.progbe.domain.terms.entity.TermsEntity;
import org.springframework.stereotype.Component;

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
}
