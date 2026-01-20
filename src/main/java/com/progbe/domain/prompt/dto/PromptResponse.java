package com.progbe.domain.prompt.dto;

import com.progbe.domain.category.dto.CategoryResponse;

import java.time.LocalDateTime;

public record PromptResponse(
        Long promptId,
        Long userId,
        CategoryResponse category,
        String title,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

