package com.progbe.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class AdminResponse {
    @Getter
    @AllArgsConstructor
    @Builder
    public static class DailyStatisticSummaryResponse {
        private List<StatisticCompareResponse> statistics;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class StatisticCompareResponse {
        private long count;
        private double changeRate;
        private String type;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class PendingReportListResponse {
       List<PendingReportResponse> pendingReports;
       int totalCount;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class PendingReportResponse {
        private String nickName;
        private String content;
        private String timeAgo;
    }
}
