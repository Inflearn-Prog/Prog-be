package com.progbe.domain.admin.service;

import com.progbe.domain.admin.dto.AdminPromptRequest;
import com.progbe.domain.admin.dto.AdminPromptResponse;
import com.progbe.domain.admin.dto.PromptAdminDto;
import com.progbe.domain.admin.mapper.AdminPromptMapper;
import com.progbe.domain.category.repository.CategoryRepository;
import com.progbe.domain.prompt.repository.PromptRepository;
import com.progbe.domain.prompt.type.PromptStatus;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminPromptService {

    private final PromptRepository promptRepository;
    private final CategoryRepository categoryRepository;
    private final AdminPromptMapper adminPromptMapper;

    @Transactional(readOnly = true)
    public AdminPromptResponse.PromptListResponse getPromptList(
            String keyword,
            Long categoryId,
            PromptStatus status,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<PromptAdminDto> promptPage = promptRepository.findByFiltersAsDto(
                keyword,
                categoryId,
                status,
                pageable
        );

        return adminPromptMapper.toPromptListResponse(promptPage);
    }

    @Transactional
    public AdminPromptResponse.BulkUpdateResponse bulkUpdatePrompts(AdminPromptRequest.BulkUpdateRequest request) {
        List<Long> existingPromptIds = validateAndGetDistinctPromptIds(request.promptIds());

        if (existingPromptIds.isEmpty()) {
            return adminPromptMapper.toBulkUpdateResponse(0);
        }

        AdminPromptRequest.UpdateFields updateFields = request.updateFields();

        if (updateFields.categoryId() != null) {
            if (categoryRepository.findByIdAndNotDeleted(updateFields.categoryId()).isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_CATEGORY);
            }
            promptRepository.bulkUpdateCategory(existingPromptIds, updateFields.categoryId());
        }

        if (updateFields.status() != null) {
            promptRepository.bulkUpdateStatus(existingPromptIds, updateFields.status());
        }

        return adminPromptMapper.toBulkUpdateResponse(existingPromptIds.size());
    }

    @Transactional
    public AdminPromptResponse.BulkDeleteResponse bulkDeletePrompts(AdminPromptRequest.BulkDeleteRequest request) {
        List<Long> existingPromptIds = validateAndGetDistinctPromptIds(request.promptIds());

        if (existingPromptIds.isEmpty()) {
            return adminPromptMapper.toBulkDeleteResponse(0);
        }

        LocalDateTime now = LocalDateTime.now();
        int deletedCount = promptRepository.bulkSoftDelete(
                existingPromptIds,
                now,
                PromptStatus.DELETED
        );

        return adminPromptMapper.toBulkDeleteResponse(deletedCount);
    }

    private List<Long> validateAndGetDistinctPromptIds(List<Long> promptIds) {
        List<Long> distinctPromptIds = promptIds.stream()
                .distinct()
                .toList();

        if (distinctPromptIds.isEmpty()) {
            return List.of();
        }

        List<Long> existingPromptIds = promptRepository.findExistingPromptIds(distinctPromptIds);
        if (existingPromptIds.isEmpty()) {
            throw new CustomException(ErrorCode.PROMPTS_NOT_FOUND);
        }

        return existingPromptIds;
    }
}
