package com.progbe.domain.user.dto;

import com.progbe.domain.user.type.CareerStatus;
import com.progbe.domain.user.type.EducationLevel;
import com.progbe.domain.user.type.JobRole;

import java.util.List;

/**
 * 사용자 프로필 응답 DTO (BasicInfo, CareerInfo, SelfIntro 세 항목으로 구성)
 *
 * @param basicInfo 기본 정보 (닉네임, 이메일, 소셜 로그인 제공자, 자기소개)
 * @param careerInfo 커리어 정보 (현재 상태, 목표 직무, 경력 연차, 학력, 전공)
 * @param selfIntro 자기소개 정보 (경험, 키워드)
 */
public record UserProfileResponse(
        BasicInfo basicInfo,
        CareerInfo careerInfo,
        SelfIntro selfIntro
) {
    public record BasicInfo(
            String nickname,
            String email,
            String provider,
            String introduction
    ) {}

    public record CareerInfo(
            List<CareerStatus> currentStatus,
            List<JobRole> targetJob,
            String careerYear,
            EducationLevel education
    ) {}

    public record SelfIntro(
            List<String> experiences,
            List<String> keywords
    ) {}
}