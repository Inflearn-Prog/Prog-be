package com.progbe.domain.report.dto;

import com.progbe.domain.report.type.ReportAction;
import jakarta.validation.constraints.Size;

public record ReportProcessRequest(
        @Size(max = 1000, message = "관리자 메모는 1000자를 초과할 수 없습니다.")
        String adminRemark,

        ReportAction action
) {
}
