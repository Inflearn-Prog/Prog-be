package com.progbe.domain.terms.dto;

import lombok.Builder;

@Builder
public record TermsResponseDto(
        Long termId,
        String title,
        Boolean isRequired
) {
}
