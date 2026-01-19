package com.progbe.domain.admin.mapper;

import com.progbe.domain.admin.dto.AdminResponse;
import com.progbe.domain.admin.entity.ReportEntity;
import com.progbe.domain.admin.entity.StatisticType;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.global.common.CommonMapper;
import com.progbe.global.common.CommonResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
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
                        .timeAgo(CommonMapper.toRelativeTime(report.getCreatedAt()))
                        .build()
                )
                .toList();

        return AdminResponse.PendingReportListResponse.builder()
                .pendingReports(reports)
                .totalCount(reports.size())
                .build();
    }
    public AdminResponse.UserSearchResult toUserSearchResult(Page<UserEntity> page)
    {

        List<AdminResponse.UserSearch> userSearchList = page.stream()
                .map(user -> AdminResponse.UserSearch.builder()
                        .nickName(user.getNickname())
                        .email(user.getEmail())
                        .status(user.getStatus().name())
                        .lastActive(CommonMapper.toRelativeTime(user.getInactivatedAt()))
                        .registered(CommonMapper.toDate(user.getCreatedAt()))
                        .build()
                )
                .toList();

        return AdminResponse.UserSearchResult.builder()
                .userSearchList(userSearchList)
                .pageInfoResponse(CommonMapper.toPageInfoResponse(page))
                .build();
    }

}

