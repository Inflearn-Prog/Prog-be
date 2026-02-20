package com.progbe.domain.admin.service;

import com.progbe.domain.admin.dto.AdminUserRequest;
import com.progbe.domain.admin.dto.AdminUserResponse;
import com.progbe.domain.admin.dto.UserCountDto;
import com.progbe.domain.admin.mapper.AdminMapper;
import com.progbe.domain.prompt.repository.PromptCommentRepository;
import com.progbe.domain.prompt.repository.PromptRepository;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.domain.user.repository.UserRepository;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final PromptRepository promptRepository;
    private final PromptCommentRepository promptCommentRepository;
    private final AdminMapper adminMapper;

    @Transactional(readOnly = true)
    public AdminUserResponse.UserListResponse getUserList(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserEntity> userPage;

        if (keyword == null || keyword.trim().isEmpty()) {
            userPage = userRepository.findAll(pageable);
        } else {
            userPage = userRepository.findByNicknameContaining(keyword.trim(), pageable);
        }

        List<Long> userIds = userPage.getContent().stream()
                .map(UserEntity::getId)
                .collect(Collectors.toList());

        if (userIds.isEmpty()) {
            return adminMapper.toUserListResponse(userPage, List.of(), List.of());
        }

        List<UserCountDto> promptCounts = promptRepository.countByUserIdsGrouped(userIds);
        List<UserCountDto> commentCounts = promptCommentRepository.countByUserIdsGrouped(userIds);

        return adminMapper.toUserListResponse(userPage, promptCounts, commentCounts);
    }

    @Transactional
    public AdminUserResponse.BulkUpdateResponse bulkUpdateStatus(AdminUserRequest.BulkStatusUpdateRequest request) {
        List<Long> distinctUserIds = validateAndGetDistinctUserIds(request.userIds());

        int updatedCount = userRepository.bulkUpdateStatus(distinctUserIds, request.status());

        return adminMapper.toBulkUpdateResponse(
                updatedCount,
                "선택한 유저들의 상태가 성공적으로 변경되었습니다."
        );
    }

    @Transactional
    public AdminUserResponse.BulkUpdateResponse bulkUpdateRole(
            Long currentUserId,
            AdminUserRequest.BulkRoleUpdateRequest request
    ) {
        List<Long> validatedUserIds = validateAndGetDistinctUserIds(request.userIds());

        if (validatedUserIds.contains(currentUserId)) {
            throw new CustomException(ErrorCode.CANNOT_CHANGE_OWN_ROLE);
        }

        int updatedCount = userRepository.bulkUpdateRole(validatedUserIds, request.role());

        return adminMapper.toBulkUpdateResponse(
                updatedCount,
                "선택한 유저들의 권한이 변경되었습니다."
        );
    }

    private List<Long> validateAndGetDistinctUserIds(List<Long> userIds) {
        List<Long> distinctUserIds = userIds.stream()
                .distinct()
                .collect(Collectors.toList());

        List<Long> existingUserIds = userRepository.findExistingUserIds(distinctUserIds);

        if (existingUserIds.size() != distinctUserIds.size()) {
            throw new CustomException(ErrorCode.DATA_INTEGRITY_ERROR);
        }

        return distinctUserIds;
    }
}