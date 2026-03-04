package com.progbe.domain.admin.category.dto;

import com.progbe.domain.category.entity.CategoryEntity;

import java.time.LocalDateTime;

public class AdminCategoryResponse {

    public record CategoryCreateResponse(
            Long categoryId,
            String name,
            String description,
            Long parentId,
            String parentName,
            LocalDateTime createdAt
    ) {
        public static CategoryCreateResponse from(CategoryEntity entity) {
            return new CategoryCreateResponse(
                    entity.getId(),
                    entity.getName(),
                    entity.getDescription(),
                    entity.getParent() != null ? entity.getParent().getId() : null,
                    entity.getParent() != null ? entity.getParent().getName() : null,
                    entity.getCreatedAt()
            );
        }
    }

    public record CategoryUpdateResponse(
            Long categoryId,
            String name,
            String description,
            Long parentId,
            String parentName,
            LocalDateTime updatedAt
    ) {
        public static CategoryUpdateResponse from(CategoryEntity entity) {
            return new CategoryUpdateResponse(
                    entity.getId(),
                    entity.getName(),
                    entity.getDescription(),
                    entity.getParent() != null ? entity.getParent().getId() : null,
                    entity.getParent() != null ? entity.getParent().getName() : null,
                    entity.getUpdatedAt()
            );
        }
    }

    public record CategoryDeleteResponse(
            String message
    ) {
        public static CategoryDeleteResponse success() {
            return new CategoryDeleteResponse("카테고리가 성공적으로 삭제되었습니다.");
        }
    }
}
