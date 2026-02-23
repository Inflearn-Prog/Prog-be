package com.progbe.domain.report.service;

import com.progbe.domain.prompt.entity.PromptEntity;
import com.progbe.domain.prompt.repository.PromptRepository;
import com.progbe.domain.report.dto.ReportCreateRequest;
import com.progbe.domain.report.dto.ReportCreateResponse;
import com.progbe.domain.report.entity.ReportEntity;
import com.progbe.domain.report.mapper.ReportMapper;
import com.progbe.domain.report.repository.ReportRepository;
import com.progbe.domain.report.type.TargetType;
import com.progbe.domain.report.validator.ReportValidator;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.domain.user.repository.UserRepository;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final PromptRepository promptRepository;
    private final UserRepository userRepository;
    private final ReportMapper reportMapper;
    private final ReportValidator reportValidator;

    @Transactional
    public ReportCreateResponse createReport(Long reporterId, ReportCreateRequest request) {
        reportValidator.validateReportCreateRequest(request);

        UserEntity reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        validateTargetAndOwnership(reporterId, request);

        if (reportRepository.existsPendingReport(
                reporterId, request.targetType(), request.targetId())) {
            throw new CustomException(ErrorCode.ALREADY_REPORTED);
        }

        ReportEntity report = reportMapper.toEntity(
                request.targetType(),
                request.targetId(),
                reporter,
                request.reason(),
                request.reasonDetail()
        );

        ReportEntity savedReport = reportRepository.save(report);
        return reportMapper.toCreateResponse(savedReport);
    }

    private void validateTargetAndOwnership(Long reporterId, ReportCreateRequest request) {
        if (request.targetType() == TargetType.PROMPT) {
            PromptEntity prompt = promptRepository.findById(request.targetId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PROMPT_NOT_FOUND));

            if (prompt.getUser().getId().equals(reporterId)) {
                throw new CustomException(ErrorCode.CANNOT_REPORT_SELF);
            }
        }
    }
}
