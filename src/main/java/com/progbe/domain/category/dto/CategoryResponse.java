package com.progbe.domain.category.dto;

import com.progbe.domain.category.entity.CategoryEntity;

public record CategoryResponse(
        Long categoryId,
        String name,
        String description
) {
    public static CategoryResponse from(CategoryEntity category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }
}

