package com.progbe.domain.terms.dto;

import lombok.Builder;

@Builder
public record TermsAgreementResponse(
        Long userId,
        Boolean isRegistrationComplete,
        String message
) {
}
