package com.progbe.domain.user.dto;

import lombok.Builder;

@Builder
public record OnboardingResponse(
        Long userId,
        String message,
        String nextStep
) {
}
