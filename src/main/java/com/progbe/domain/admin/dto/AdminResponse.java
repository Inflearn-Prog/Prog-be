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
}
