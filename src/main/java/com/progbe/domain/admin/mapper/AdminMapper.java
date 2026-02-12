package com.progbe.domain.admin.mapper;

import com.progbe.domain.admin.dto.AdminResponse;
import com.progbe.domain.admin.dto.AdminUserResponse;
import com.progbe.domain.admin.dto.UserCountDto;
import com.progbe.domain.admin.type.MetricCalculationStatus;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.global.common.CommonMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
public class AdminMapper {

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
            double percentage,
            MetricCalculationStatus status
    ) {
        return AdminResponse.MetricInfo.builder()
                .count(count)
                .increment(increment)
                .percentage(percentage)
                .status(status.name())
                .build();
    }

    public AdminUserResponse.UserListResponse toUserListResponse(
            Page<UserEntity> userPage,
            List<UserCountDto> promptCounts,
            List<UserCountDto> commentCounts
    ) {
        Map<Long, Long> promptCountMap = promptCounts.stream()
                .collect(Collectors.toMap(UserCountDto::getUserId, UserCountDto::getCount));

        Map<Long, Long> commentCountMap = commentCounts.stream()
                .collect(Collectors.toMap(UserCountDto::getUserId, UserCountDto::getCount));

        List<AdminUserResponse.UserInfo> userInfoList = userPage.getContent().stream()
                .map(user -> AdminUserResponse.UserInfo.builder()
                        .userId(user.getId())
                        .nickname(user.getNickname())
                        .role(user.getRole())
                        .promptCount(promptCountMap.getOrDefault(user.getId(), 0L))
                        .commentCount(commentCountMap.getOrDefault(user.getId(), 0L))
                        .status(user.getStatus())
                        .build()
                )
                .collect(Collectors.toList());

        return AdminUserResponse.UserListResponse.builder()
                .content(userInfoList)
                .pageInfo(CommonMapper.toPageInfoResponse(userPage))
                .build();
    }

    public AdminUserResponse.BulkUpdateResponse toBulkUpdateResponse(int updatedCount, String message) {
        return AdminUserResponse.BulkUpdateResponse.builder()
                .updatedCount(updatedCount)
                .message(message)
                .build();
    }
}

