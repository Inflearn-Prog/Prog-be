package com.progbe.domain.user.dto;

import com.progbe.domain.user.type.EducationLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record OnboardingBasicRequest(
        @NotNull(message = "학력은 필수 선택 항목입니다.")
        EducationLevel educationLevel,

        @NotBlank(message = "전공은 필수 입력 항목입니다.")
        String major,

        @PositiveOrZero(message = "경력은 0 이상의 숫자여야 합니다.")
        Integer careerYears
) {
        public OnboardingBasicRequest {
                if (careerYears == null) {
                        careerYears = 0;
                }
        }
}