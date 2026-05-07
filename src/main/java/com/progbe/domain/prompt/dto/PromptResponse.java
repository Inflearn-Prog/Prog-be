package com.progbe.domain.prompt.dto;

import com.progbe.domain.category.dto.CategoryResponse;

import java.time.LocalDateTime;

public record PromptResponse(
        Long promptId,
        Long userId,
        CategoryResponse category,
        String title,
        String content,
        String userName,
        String userIcon,
        String userDesc,
        Boolean isLiked,
        Integer likes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

