package com.progbe.domain.terms.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserAgreedTermDto(
        Long termId,
        String title,
        Boolean isRequired,
        LocalDateTime agreedAt
) {
}
