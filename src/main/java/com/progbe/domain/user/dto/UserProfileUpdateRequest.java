package com.progbe.domain.user.dto;

import com.progbe.domain.user.type.CareerStatus;
import com.progbe.domain.user.type.EducationLevel;
import com.progbe.domain.user.type.JobRole;

import java.util.List;

/**
 * Request에서 null로 전달된 값은 변경 없음 (무시)
 * 명시적으로 빈 문자열("")이나 빈 리스트([])로 전달된 경우 삭제로 처리
 */
public record UserProfileUpdateRequest(
        BasicInfo basicInfo,
        CareerInfo careerInfo,
        SelfIntro selfIntro
) {

    public record BasicInfo(
            String nickname,
            String introduction
    ) {}

    public record CareerInfo(
            List<CareerStatus> currentStatuses,
            List<JobRole> targetJobRoles,
            Integer careerYears,
            EducationLevel educationLevel
    ) {}

    public record SelfIntro(
            List<String> experiences,
            List<String> keywords
    ) {}
}