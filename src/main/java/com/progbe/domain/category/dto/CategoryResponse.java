package com.progbe.domain.category.dto;

public record CategoryResponse(
        Long categoryId,
        String name,
        String description
) {
}

