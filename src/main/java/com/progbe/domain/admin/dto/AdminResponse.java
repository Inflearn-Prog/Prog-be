package com.progbe.domain.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class AdminResponse {
    @Getter
    @AllArgsConstructor
    @Builder
    public static class StatsSummaryResponse {
        private PeriodInfo period;
        private MetricInfo newUsers;
        private MetricInfo newPrompts;
        private MetricInfo copyCount;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class PeriodInfo {
        private String startDate;
        private String endDate;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class MetricInfo {
        private long count;
        private long increment;
        private double percentage;
        private String status;
    }
}
