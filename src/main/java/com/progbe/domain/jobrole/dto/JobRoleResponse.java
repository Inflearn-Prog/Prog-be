package com.progbe.domain.jobrole.dto;

import com.progbe.domain.jobrole.entity.JobRoleEntity;

public record JobRoleResponse(
        Long id,
        String name,
        int depth,
        Long parentId
) {
    public static JobRoleResponse from(JobRoleEntity entity) {
        return new JobRoleResponse(
                entity.getId(),
                entity.getName(),
                entity.getDepth(),
                entity.getParent() != null ? entity.getParent().getId() : null
        );
    }
}
