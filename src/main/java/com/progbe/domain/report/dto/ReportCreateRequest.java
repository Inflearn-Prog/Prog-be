package com.progbe.domain.report.dto;

import com.progbe.domain.report.type.ReportReason;
import com.progbe.domain.report.type.TargetType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportCreateRequest(
        @NotNull(message = "신고 대상 타입을 입력해주세요.")
        TargetType targetType,

        @NotNull(message = "신고 대상 ID를 입력해주세요.")
        Long targetId,

        @NotNull(message = "신고 사유를 선택해주세요.")
        ReportReason reason,

        @Size(max = 200, message = "상세 사유는 200자를 초과할 수 없습니다.")
        String reasonDetail
) {
}
