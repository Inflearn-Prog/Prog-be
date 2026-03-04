package com.progbe.domain.admin.category.controller;

import com.progbe.domain.admin.category.dto.AdminCategoryRequest;
import com.progbe.domain.admin.category.dto.AdminCategoryResponse;
import com.progbe.domain.admin.category.service.AdminCategoryService;
import com.progbe.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<AdminCategoryResponse.CategoryCreateResponse>> createCategory(
            @Valid @RequestBody AdminCategoryRequest.CreateRequest request
    ) {
        AdminCategoryResponse.CategoryCreateResponse response = adminCategoryService.createCategory(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<AdminCategoryResponse.CategoryUpdateResponse>> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody AdminCategoryRequest.UpdateRequest request
    ) {
        AdminCategoryResponse.CategoryUpdateResponse response = adminCategoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<AdminCategoryResponse.CategoryDeleteResponse>> deleteCategory(
            @PathVariable Long categoryId
    ) {
        AdminCategoryResponse.CategoryDeleteResponse response = adminCategoryService.deleteCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
