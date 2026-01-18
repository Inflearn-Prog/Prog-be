package com.progbe.domain.admin.mapper;

import com.progbe.domain.admin.dto.AdminResponse;
import com.progbe.domain.admin.entity.ReportEntity;
import com.progbe.domain.admin.entity.StatisticType;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Component
public class AdminMapper {
    public AdminResponse.DailyStatisticSummaryResponse toDailyStatisticSummaryResponse(
            Map<StatisticType, Double> rateMap,
            Map<StatisticType, Long> todayCountMap
    ) {
        List<AdminResponse.StatisticCompareResponse> responses = new ArrayList<>();

        for (StatisticType type : rateMap.keySet()) {

            responses.add(
                    AdminResponse.StatisticCompareResponse.builder()
                            .type(type.toString())
                            .count(todayCountMap.get(type))
                            .changeRate(rateMap.get(type))
                            .build()
            );
        }

        return AdminResponse.DailyStatisticSummaryResponse.builder()
                .statistics(responses)
                .build();
    }

    public AdminResponse.PendingReportListResponse toPendingReportListResponse(List<ReportEntity> pendingReports)
    {
        List<AdminResponse.PendingReportResponse> reports = pendingReports.stream()
                .map(report -> AdminResponse.PendingReportResponse.builder()
                        .nickName(report.getNickName())
                        .content(report.getContent())
                        .timeAgo(toRelativeTime(report.getCreatedAt()))
                        .build()
                )
                .toList();

        return AdminResponse.PendingReportListResponse.builder()
                .pendingReports(reports)
                .totalCount(reports.size())
                .build();
    }

    private static String toRelativeTime(LocalDateTime time) {
        Duration duration = Duration.between(time, LocalDateTime.now());

        if (duration.toHours() < 24) return duration.toHours() + "hrs ago";
        return duration.toDays() + "day ago";
    }
}
