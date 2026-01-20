package com.progbe.domain.category.dto;

import java.util.List;

public record CategoryListResponse(
        List<CategoryResponse> categories
) {
}

