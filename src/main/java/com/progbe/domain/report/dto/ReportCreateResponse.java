package com.progbe.domain.report.dto;

import com.progbe.domain.report.type.TargetType;

import java.time.LocalDateTime;

public record ReportCreateResponse(
        TargetType targetType,
        Long targetId,
        Long reporterId,
        LocalDateTime createdAt
) {
}
