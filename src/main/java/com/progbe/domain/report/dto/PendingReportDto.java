package com.progbe.domain.report.dto;

import com.progbe.domain.report.type.ReportReason;
import com.progbe.domain.report.type.ReportStatus;

import java.time.LocalDateTime;

public record PendingReportDto(
        Long reportId,
        LocalDateTime reportedAt,
        ReportReason reasonCategory,
        String targetPromptTitle,
        String reporterNickname,
        String reportContent,
        ReportStatus status
) {
}
