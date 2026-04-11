package com.progbe.domain.admin.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class AdminCategoryRequest {

    public record CreateRequest(
            @NotBlank(message = "카테고리 이름은 필수입니다.")
            @Size(max = 50, message = "카테고리 이름은 최대 50자까지 입력 가능합니다.")
            String name,

            @Size(max = 500, message = "카테고리 설명은 최대 500자까지 입력 가능합니다.")
            String description,

            Long parentId
    ) {
    }

    public record UpdateRequest(
            @NotBlank(message = "카테고리 이름은 필수입니다.")
            @Size(max = 50, message = "카테고리 이름은 최대 50자까지 입력 가능합니다.")
            String name,

            @Size(max = 500, message = "카테고리 설명은 최대 500자까지 입력 가능합니다.")
            String description,

            Long parentId
    ) {
    }

    public record UpdateOrderRequest(
            @NotNull(message = "카테고리 ID 목록은 필수입니다.")
            @Size(min = 1, message = "최소 1개 이상의 카테고리 ID가 필요합니다.")
            List<Long> categoryIds
    ) {
    }
}
