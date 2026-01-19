package com.progbe.domain.admin.dto;

import com.progbe.global.common.CommonResponse;
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

    @Getter
    @AllArgsConstructor
    @Builder
    public static  class UserSearchResult
    {
        List<UserSearch> userSearchList;
        CommonResponse.PageInfoResponse pageInfoResponse;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static  class UserSearch
    {
        String nickName;
        String email;
        String status;
        String lastActive;
        String registered;
        //액션스??
    }
}
