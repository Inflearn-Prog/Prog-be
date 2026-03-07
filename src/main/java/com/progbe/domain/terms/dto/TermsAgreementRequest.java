package com.progbe.domain.terms.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TermsAgreementRequest(
        @NotNull(message = "동의할 약관 ID 목록은 필수입니다.")
        @NotEmpty(message = "최소 1개 이상의 약관에 동의해야 합니다.")
        List<Long> agreedTermIds
) {
}
