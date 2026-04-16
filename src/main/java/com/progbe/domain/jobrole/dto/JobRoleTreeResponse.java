package com.progbe.domain.jobrole.dto;

import com.progbe.domain.jobrole.entity.JobRoleEntity;

import java.util.ArrayList;
import java.util.List;

public record JobRoleTreeResponse(
        Long id,
        String name,
        int depth,
        int displayOrder,
        boolean selectable,
        List<JobRoleTreeResponse> children
) {
    public static JobRoleTreeResponse from(JobRoleEntity entity) {
        return new JobRoleTreeResponse(
                entity.getId(),
                entity.getName(),
                entity.getDepth(),
                entity.getDisplayOrder(),
                entity.isSelectable(),
                new ArrayList<>()
        );
    }
}
