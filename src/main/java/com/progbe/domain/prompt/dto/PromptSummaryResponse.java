package com.progbe.domain.prompt.dto;

import com.progbe.domain.category.dto.CategoryResponse;

import java.time.LocalDateTime;

public record PromptSummaryResponse(
        Long promptId,
        Long userId,
        String nickname,
        CategoryResponse category,
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean isLiked
) {
}

