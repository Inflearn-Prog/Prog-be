package com.progbe.domain.admin.category.service;

import com.progbe.domain.admin.category.dto.AdminCategoryRequest;
import com.progbe.domain.admin.category.dto.AdminCategoryResponse;
import com.progbe.domain.category.entity.CategoryEntity;
import com.progbe.domain.category.repository.CategoryRepository;
import com.progbe.domain.prompt.repository.PromptRepository;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCategoryService {

    private final CategoryRepository categoryRepository;
    private final PromptRepository promptRepository;

    @Transactional
    public AdminCategoryResponse.CategoryCreateResponse createCategory(AdminCategoryRequest.CreateRequest request) {
        if (categoryRepository.findByNameAndNotDeleted(request.name()).isPresent()) {
            throw new CustomException(ErrorCode.CATEGORY_NAME_DUPLICATE);
        }

        CategoryEntity parent = null;
        if (request.parentId() != null) {
            parent = categoryRepository.findByIdAndNotDeleted(request.parentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_PARENT_CATEGORY));

            if (!parent.isParentCategory()) {
                throw new CustomException(ErrorCode.CATEGORY_DEPTH_EXCEEDED);
            }
        }

        CategoryEntity category = CategoryEntity.builder()
                .name(request.name())
                .description(request.description())
                .parent(parent)
                .build();

        try {
            CategoryEntity savedCategory = categoryRepository.save(category);
            return AdminCategoryResponse.CategoryCreateResponse.from(savedCategory);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.CATEGORY_NAME_DUPLICATE);
        }
    }

    @Transactional
    public AdminCategoryResponse.CategoryUpdateResponse updateCategory(
            Long categoryId,
            AdminCategoryRequest.UpdateRequest request
    ) {
        CategoryEntity category = categoryRepository.findByIdAndNotDeleted(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        if (!category.getName().equals(request.name())) {
            categoryRepository.findByNameAndNotDeleted(request.name())
                    .ifPresent(existing -> {
                        throw new CustomException(ErrorCode.CATEGORY_NAME_DUPLICATE);
                    });
        }

        CategoryEntity parent = null;
        if (request.parentId() != null) {
            if (request.parentId().equals(categoryId)) {
                throw new CustomException(ErrorCode.INVALID_PARENT_CATEGORY);
            }

            parent = categoryRepository.findByIdAndNotDeleted(request.parentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_PARENT_CATEGORY));

            if (!parent.isParentCategory()) {
                throw new CustomException(ErrorCode.CATEGORY_DEPTH_EXCEEDED);
            }

            if (categoryRepository.existsChildrenByParentId(categoryId)) {
                throw new CustomException(ErrorCode.CATEGORY_DEPTH_EXCEEDED);
            }
        }

        try {
            category.update(request.name(), request.description(), parent);
            return AdminCategoryResponse.CategoryUpdateResponse.from(category);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.CATEGORY_NAME_DUPLICATE);
        }
    }

    @Transactional
    public AdminCategoryResponse.CategoryDeleteResponse deleteCategory(Long categoryId) {
        CategoryEntity category = categoryRepository.findByIdAndNotDeleted(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        if (categoryRepository.existsChildrenByParentId(categoryId)) {
            throw new CustomException(ErrorCode.CATEGORY_NOT_EMPTY);
        }

        if (promptRepository.existsByCategoryIdAndNotDeleted(categoryId)) {
            throw new CustomException(ErrorCode.CATEGORY_NOT_EMPTY);
        }

        category.delete();
        return AdminCategoryResponse.CategoryDeleteResponse.success();
    }
}
