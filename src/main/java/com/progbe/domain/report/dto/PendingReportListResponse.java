package com.progbe.domain.report.dto;

import java.util.List;

public record PendingReportListResponse(
        List<PendingReportDto> content,
        PageInfo pageInfo
) {
    public record PageInfo(
            int currentPage,
            int size,
            long totalElements,
            int totalPages
    ) {
    }
}
