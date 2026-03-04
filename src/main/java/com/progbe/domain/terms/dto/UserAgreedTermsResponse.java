package com.progbe.domain.terms.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record UserAgreedTermsResponse(
        Long userId,
        List<UserAgreedTermDto> agreedTerms
) {
}
