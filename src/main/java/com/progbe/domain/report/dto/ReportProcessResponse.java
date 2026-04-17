package com.progbe.domain.report.dto;

import com.progbe.domain.report.type.TargetType;

import java.time.LocalDateTime;

public record ReportProcessResponse(
        Long reportId,
        Long targetId,
        TargetType targetType,
        LocalDateTime processedAt
) {
}
