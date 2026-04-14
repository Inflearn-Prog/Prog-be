package com.progbe.domain.jobrole.service;

import com.progbe.domain.jobrole.dto.JobRoleResponse;
import com.progbe.domain.jobrole.dto.JobRoleTreeResponse;
import com.progbe.domain.jobrole.entity.JobRoleEntity;
import com.progbe.domain.jobrole.entity.UserJobRoleEntity;
import com.progbe.domain.jobrole.repository.JobRoleRepository;
import com.progbe.domain.jobrole.repository.UserJobRoleRepository;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.domain.user.repository.UserRepository;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JobRoleService {

    private final JobRoleRepository jobRoleRepository;
    private final UserJobRoleRepository userJobRoleRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<JobRoleTreeResponse> getFullTree() {
        List<JobRoleEntity> all = jobRoleRepository.findAllActive(); // 쿼리-1회
        return buildTree(all);
    }

    @Transactional
    public void saveUserJobRoles(Long userId, List<Long> jobRoleIds) {
        if (jobRoleIds == null || jobRoleIds.isEmpty()) {
            userJobRoleRepository.deleteByUserId(userId);
            return;
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<JobRoleEntity> jobRoles = jobRoleRepository.findAllActiveByIds(jobRoleIds);
        if (jobRoles.size() != jobRoleIds.size()) {
            throw new CustomException(ErrorCode.JOB_ROLE_NOT_FOUND);
        }

        boolean hasNonSelectable = jobRoles.stream().anyMatch(j -> !j.isSelectable());
        if (hasNonSelectable) {
            throw new CustomException(ErrorCode.JOB_ROLE_NOT_SELECTABLE);
        }

        userJobRoleRepository.deleteByUserId(userId);
        List<UserJobRoleEntity> mappings = jobRoles.stream()
                .map(jobRole -> UserJobRoleEntity.of(user, jobRole))
                .toList();
        userJobRoleRepository.saveAll(mappings);
    }

    @Transactional(readOnly = true)
    public List<JobRoleResponse> getUserJobRoles(Long userId) {
        return userJobRoleRepository.findByUserIdWithJobRole(userId)
                .stream()
                .map(ujr -> JobRoleResponse.from(ujr.getJobRole()))
                .toList();
    }

    // 플랫 리스트 → 트리 조립 (depth ASC 정렬로 인해 부모가 먼저 처리됨)
    private List<JobRoleTreeResponse> buildTree(List<JobRoleEntity> all) {
        Map<Long, JobRoleTreeResponse> map = new LinkedHashMap<>();
        for (JobRoleEntity entity : all) {
            map.put(entity.getId(), JobRoleTreeResponse.from(entity));
        }

        List<JobRoleTreeResponse> roots = new ArrayList<>();
        for (JobRoleEntity entity : all) {
            JobRoleTreeResponse node = map.get(entity.getId());
            if (entity.getParent() == null) {
                roots.add(node);
            } else {
                JobRoleTreeResponse parent = map.get(entity.getParent().getId());
                if (parent != null) {
                    parent.children().add(node);
                }
            }
        }
        return roots;
    }

}
