package com.progbe.domain.admin.category.dto;

import com.progbe.domain.category.entity.CategoryEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    public record CategoryOrderUpdateResponse(
            int updatedCount,
            String message
    ) {
        public static CategoryOrderUpdateResponse success(int count) {
            return new CategoryOrderUpdateResponse(count, "카테고리 순서가 성공적으로 변경되었습니다.");
        }
    }

    public record CategoryListResponse(
            List<CategoryInfo> categories
    ) {
    }

    public record CategoryInfo(
            Long categoryId,
            String name,
            String description,
            Long parentId,
            String parentName,
            int childrenCount,
            int displayOrder,
            String createdAt
    ) {
        public static CategoryInfo from(CategoryEntity entity) {
            long childCount = entity.getChildren().stream()
                    .filter(c -> c.getDeletedAt() == null)
                    .count();
            return new CategoryInfo(
                    entity.getId(),
                    entity.getName(),
                    entity.getDescription(),
                    entity.getParent() != null ? entity.getParent().getId() : null,
                    entity.getParent() != null ? entity.getParent().getName() : null,
                    (int) childCount,
                    entity.getDisplayOrder(),
                    entity.getCreatedAt().format(DateTimeFormatter.ofPattern("yy.MM.dd"))
            );
        }
    }
}
