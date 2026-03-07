package com.progbe.domain.terms.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TermsWithdrawalRequest(
        @NotNull(message = "약관 ID 목록은 필수입니다.")
        @NotEmpty(message = "최소 1개 이상의 약관 ID가 필요합니다.")
        List<Long> termIds
) {
}
