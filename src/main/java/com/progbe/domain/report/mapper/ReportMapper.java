package com.progbe.domain.report.mapper;

import com.progbe.domain.report.dto.PendingReportDto;
import com.progbe.domain.report.dto.ReportCreateResponse;
import com.progbe.domain.report.dto.ReportProcessResponse;
import com.progbe.domain.report.entity.ReportEntity;
import com.progbe.domain.report.type.ReportReason;
import com.progbe.domain.report.type.TargetType;
import com.progbe.domain.user.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class ReportMapper {

    public ReportEntity toEntity(TargetType targetType, Long targetId, UserEntity reporter,
                                 ReportReason reason, String reasonDetail) {
        return ReportEntity.builder()
                .targetType(targetType)
                .targetId(targetId)
                .reporter(reporter)
                .reason(reason)
                .reasonDetail(reasonDetail)
                .build();
    }

    public ReportCreateResponse toCreateResponse(ReportEntity report) {
        return new ReportCreateResponse(
                report.getTargetType(),
                report.getTargetId(),
                report.getReporter().getId(),
                report.getCreatedAt()
        );
    }

    public PendingReportDto toPendingReportDto(ReportEntity report, String targetPromptTitle, String reporterNickname) {
        String reportContent = report.getReason() == ReportReason.OTHER
                ? report.getReasonDetail()
                : report.getReason().getDisplayText();

        return new PendingReportDto(
                report.getId(),
                report.getCreatedAt(),
                report.getReason(),
                targetPromptTitle,
                reporterNickname,
                reportContent,
                report.getStatus()
        );
    }

    public ReportProcessResponse toProcessResponse(ReportEntity report) {
        return new ReportProcessResponse(
                report.getId(),
                report.getTargetId(),
                report.getTargetType().name(),
                report.getProcessedAt()
        );
    }
}
