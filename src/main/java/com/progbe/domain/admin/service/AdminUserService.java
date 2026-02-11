package com.progbe.domain.admin.service;

import com.progbe.domain.admin.dto.AdminUserRequest;
import com.progbe.domain.admin.dto.AdminUserResponse;
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
import java.util.Map;
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
            return adminMapper.toUserListResponse(userPage, Map.of(), Map.of());
        }

        Map<Long, Long> promptCountMap = getPromptCountMap(userIds);
        Map<Long, Long> commentCountMap = getCommentCountMap(userIds);

        return adminMapper.toUserListResponse(userPage, promptCountMap, commentCountMap);
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
        List<Long> distinctUserIds = request.userIds().stream()
                .distinct()
                .toList();

        if (distinctUserIds.contains(currentUserId)) {
            throw new CustomException(ErrorCode.CANNOT_CHANGE_OWN_ROLE);
        }

        List<Long> validatedUserIds = validateAndGetDistinctUserIds(request.userIds());

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

    private Map<Long, Long> getPromptCountMap(List<Long> userIds) {
        List<Object[]> results = promptRepository.countByUserIdsGrouped(userIds);
        Map<Long, Long> countMap = new java.util.HashMap<>();
        
        for (Object[] result : results) {
            Long userId = (Long) result[0];
            Long count = (Long) result[1];
            countMap.put(userId, count);
        }
        
        for (Long userId : userIds) {
            countMap.putIfAbsent(userId, 0L);
        }
        
        return countMap;
    }

    private Map<Long, Long> getCommentCountMap(List<Long> userIds) {
        List<Object[]> results = promptCommentRepository.countByUserIdsGrouped(userIds);
        Map<Long, Long> countMap = new java.util.HashMap<>();
        
        for (Object[] result : results) {
            Long userId = (Long) result[0];
            Long count = (Long) result[1];
            countMap.put(userId, count);
        }
        
        for (Long userId : userIds) {
            countMap.putIfAbsent(userId, 0L);
        }
        
        return countMap;
    }
}