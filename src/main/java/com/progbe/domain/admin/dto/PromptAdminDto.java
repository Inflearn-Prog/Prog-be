package com.progbe.domain.admin.dto;

import com.progbe.domain.prompt.type.PromptStatus;

import java.time.LocalDateTime;

/**
 * 관리자 프롬프트 조회용 DTO
 * N+1 문제 방지를 위해 JPQL에서 직접 조회하여 사용
 */
public record PromptAdminDto(
        Long promptId,
        String title,
        String authorNickname,
        String categoryName,
        PromptStatus status,
        LocalDateTime createdAt
) {
}
