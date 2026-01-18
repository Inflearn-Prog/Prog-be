package com.progbe.domain.category.controller;

import com.progbe.domain.category.dto.CategoryListResponse;
import com.progbe.domain.category.service.CategoryService;
import com.progbe.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<CategoryListResponse>> getCategories() {
        CategoryListResponse response = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

