package com.progbe.domain.user.dto;

import com.progbe.domain.user.type.CareerStatus;
import com.progbe.domain.user.type.JobRole;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OnboardingCareerRequest(
        @NotEmpty(message = "현재 상태는 필수 선택 항목입니다.")
        List<CareerStatus> currentStatuses,

        @NotEmpty(message = "목표 직무는 필수 선택 항목입니다.")
        List<JobRole> targetJobRoles
) {
}
