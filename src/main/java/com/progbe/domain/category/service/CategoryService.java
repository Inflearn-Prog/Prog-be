package com.progbe.domain.category.service;

import com.progbe.domain.category.dto.CategoryListResponse;
import com.progbe.domain.category.dto.CategoryResponse;
import com.progbe.domain.category.entity.CategoryEntity;
import com.progbe.domain.category.mapper.CategoryMapper;
import com.progbe.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryListResponse getAllCategories() {
        List<CategoryEntity> categories = categoryRepository.findAllByNotDeleted();
        List<CategoryResponse> categoryResponses = categoryMapper.toCategoryResponseList(categories);
        return new CategoryListResponse(categoryResponses);
    }
}

