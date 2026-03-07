package com.progbe.domain.terms.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record TermsWithdrawalResponse(
        Long userId,
        List<Long> withdrawnTermIds,
        String message
) {
}
