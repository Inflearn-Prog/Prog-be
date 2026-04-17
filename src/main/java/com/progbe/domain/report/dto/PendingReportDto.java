package com.progbe.domain.report.dto;

import com.progbe.domain.report.type.ReportReason;
import com.progbe.domain.report.type.ReportStatus;

import java.time.LocalDateTime;

public record PendingReportDto(
        Long reportId,
        LocalDateTime reportedAt,
        ReportReason reason,
        String targetTitle,
        String reporterNickname,
        String reasonContent,
        ReportStatus status
) {
}
