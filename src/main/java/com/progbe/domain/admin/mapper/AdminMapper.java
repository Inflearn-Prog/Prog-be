package com.progbe.domain.admin.mapper;

import com.progbe.domain.admin.dto.AdminResponse;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.global.common.CommonMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;


@Component
public class AdminMapper {

    // FIXME - start ============================
    public AdminResponse.UserSearchResult toUserSearchResult(Page<UserEntity> page) {

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
    // FIXME - end ============================

    public AdminResponse.StatsSummaryResponse toStatsSummaryResponse(
            LocalDate startDate,
            LocalDate endDate,
            AdminResponse.MetricInfo newUsers,
            AdminResponse.MetricInfo newPrompts,
            AdminResponse.MetricInfo copyCount
    ) {
        return AdminResponse.StatsSummaryResponse.builder()
                .period(AdminResponse.PeriodInfo.builder()
                        .startDate(startDate.toString())
                        .endDate(endDate.toString())
                        .build())
                .newUsers(newUsers)
                .newPrompts(newPrompts)
                .copyCount(copyCount)
                .build();
    }

    public AdminResponse.MetricInfo toMetricInfo(
            long count,
            long increment,
            double percentage
    ) {
        return AdminResponse.MetricInfo.builder()
                .count(count)
                .increment(increment)
                .percentage(percentage)
                .build();
    }
}

