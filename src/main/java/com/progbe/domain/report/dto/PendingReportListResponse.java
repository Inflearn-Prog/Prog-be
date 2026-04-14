package com.progbe.domain.report.dto;

import com.progbe.global.common.CommonResponse;

import java.util.List;

public record PendingReportListResponse(
        List<PendingReportDto> content,
        CommonResponse.PageInfoResponse pageInfo
) {
}
