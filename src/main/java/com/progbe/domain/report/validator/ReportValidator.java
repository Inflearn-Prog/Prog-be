package com.progbe.domain.report.validator;

import com.progbe.domain.report.dto.ReportCreateRequest;
import com.progbe.domain.report.type.ReportReason;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import org.springframework.stereotype.Component;

@Component
public class ReportValidator {

    public void validateReportCreateRequest(ReportCreateRequest request) {
        if (request.reason() == ReportReason.OTHER) {
            if (request.reasonDetail() == null || request.reasonDetail().trim().isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }

        if (request.reasonDetail() != null && request.reasonDetail().length() > 200) {
            throw new CustomException(ErrorCode.TEXT_TOO_LONG);
        }
    }
}
