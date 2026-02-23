package com.progbe.domain.prompt.dto;

import com.progbe.domain.category.dto.CategoryResponse;
import com.progbe.domain.prompt.entity.PromptEntity;

import java.time.LocalDateTime;

public record PromptSummaryResponse(
        Long promptId,
        CategoryResponse category,
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PromptSummaryResponse of(PromptEntity promptEntity) {
        return new PromptSummaryResponse(
                promptEntity.getId(),
                CategoryResponse.from(promptEntity.getCategory()),
                promptEntity.getTitle(),
                promptEntity.getCreatedAt(),
                promptEntity.getUpdatedAt()
        );
    }
}

