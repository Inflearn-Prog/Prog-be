package com.progbe.domain.admin.dto;

import com.progbe.global.common.CommonResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class AdminResponse {

    // FIXME - start ============================
    @Getter
    @AllArgsConstructor
    @Builder
    public static class UserSearchResult {
        List<UserSearch> userSearchList;
        CommonResponse.PageInfoResponse pageInfoResponse;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class UserSearch {
        String nickName;
        String email;
        String status;
        String lastActive;
        String registered;
    }
    // FIXME - end ============================

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
