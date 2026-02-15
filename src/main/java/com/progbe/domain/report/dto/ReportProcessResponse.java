package com.progbe.domain.report.dto;

import java.time.LocalDateTime;

public record ReportProcessResponse(
        Long reportId,
        Long targetId,
        String targetType,
        LocalDateTime processedAt
) {
}
