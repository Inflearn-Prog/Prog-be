package com.progbe.domain.report.service;

import com.progbe.domain.prompt.entity.PromptEntity;
import com.progbe.domain.prompt.repository.PromptRepository;
import com.progbe.domain.prompt.type.PromptStatus;
import com.progbe.domain.report.dto.PendingReportDto;
import com.progbe.domain.report.dto.PendingReportListResponse;
import com.progbe.domain.report.dto.ReportProcessRequest;
import com.progbe.domain.report.dto.ReportProcessResponse;
import com.progbe.domain.report.entity.ReportEntity;
import com.progbe.domain.report.mapper.ReportMapper;
import com.progbe.domain.report.repository.ReportRepository;
import com.progbe.domain.report.type.ReportStatus;
import com.progbe.domain.report.type.TargetType;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.domain.user.repository.UserRepository;
import com.progbe.domain.user.type.UserStatus;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final ReportRepository reportRepository;
    private final PromptRepository promptRepository;
    private final UserRepository userRepository;
    private final ReportMapper reportMapper;

    @Transactional(readOnly = true)
    public PendingReportListResponse getPendingReports(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PendingReportDto> reportPage = reportRepository.findPendingReportsAsDto(
                ReportStatus.PENDING, pageable);

        PendingReportListResponse.PageInfo pageInfo = new PendingReportListResponse.PageInfo(
                reportPage.getNumber(),
                reportPage.getSize(),
                reportPage.getTotalElements(),
                reportPage.getTotalPages()
        );

        return new PendingReportListResponse(reportPage.getContent(), pageInfo);
    }

    @Transactional
    public ReportProcessResponse processReport(Long reportId, ReportProcessRequest request) {
        ReportEntity report = reportRepository.findByIdWithLock(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        if (report.getStatus() == ReportStatus.PROCESSED) {
            throw new CustomException(ErrorCode.ALREADY_PROCESSED);
        }

        if (report.getTargetType() == TargetType.PROMPT) {
            processPromptReport(report);
        }

        report.process(request.adminRemark());
        reportRepository.save(report);

        return reportMapper.toProcessResponse(report);
    }

    private void processPromptReport(ReportEntity report) {
        Optional<PromptEntity> promptOpt = promptRepository.findById(report.getTargetId());

        if (promptOpt.isEmpty()) {
            return;
        }

        PromptEntity prompt = promptOpt.get();
        UserEntity author = prompt.getUser();

        prompt.updateStatus(PromptStatus.PRIVATE);
        promptRepository.save(prompt);

        author.updateStatus(UserStatus.SUSPENDED);
        userRepository.save(author);
    }

}
