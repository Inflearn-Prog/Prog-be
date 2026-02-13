package com.progbe.domain.admin.service;

import com.progbe.domain.admin.component.StatisticComponent;
import com.progbe.domain.admin.dto.AdminResponse;
import com.progbe.domain.admin.entity.StatisticType;
import com.progbe.domain.admin.mapper.AdminMapper;
import com.progbe.domain.admin.repository.StatisticRepository;
import com.progbe.domain.user.repository.UserRepository;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final StatisticRepository statisticRepository;
    private final UserRepository userRepository;
    private final StatisticComponent statisticComponent;
    private final AdminMapper adminMapper;

    // FIXME - start ============================
    public AdminResponse.UserSearchResult searchUsers(String keyword, int page) {
        Pageable pageable = PageRequest.of(page, 5);
        return adminMapper.toUserSearchResult(userRepository.findByEmailContainingOrNicknameContaining(keyword, keyword, pageable));
    }
    // FIXME - end ============================

    @Transactional(readOnly = true)
    public AdminResponse.StatsSummaryResponse getStatsSummary(LocalDate startDate, LocalDate endDate) {

        validateStatisticsReady(endDate);

        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        LocalDate prevStartDate = startDate.minusDays(daysBetween);
        LocalDate prevEndDate = endDate.minusDays(daysBetween);

        var currentCounts = statisticRepository.sumByTypeGrouped(
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay()
        );
        var previousCounts = statisticRepository.sumByTypeGrouped(
                prevStartDate.atStartOfDay(),
                prevEndDate.plusDays(1).atStartOfDay()
        );

        AdminResponse.MetricInfo newUsers = buildMetricInfo(
                StatisticType.NEW_USERS, currentCounts, previousCounts
        );
        AdminResponse.MetricInfo newPrompts = buildMetricInfo(
                StatisticType.NEW_PROMPTS, currentCounts, previousCounts
        );
        AdminResponse.MetricInfo copyCount = buildMetricInfo(
                StatisticType.COPY_COUNT, currentCounts, previousCounts
        );

        return adminMapper.toStatsSummaryResponse(
                startDate,
                endDate,
                newUsers,
                newPrompts,
                copyCount
        );
    }

    private void validateStatisticsReady(LocalDate endDate) {
        LocalDateTime startOfDay = endDate.atStartOfDay();
        LocalDateTime endOfDay = endDate.plusDays(1).atStartOfDay();

        long existingTypeCount = statisticRepository.countDistinctTypesByDate(startOfDay, endOfDay);

        if (existingTypeCount < StatisticType.values().length) {
            throw new CustomException(ErrorCode.STATISTICS_NOT_READY);
        }
    }

    private AdminResponse.MetricInfo buildMetricInfo(
            StatisticType type,
            java.util.Map<StatisticType, Long> currentCounts,
            java.util.Map<StatisticType, Long> previousCounts
    ) {
        long currentCount = currentCounts.getOrDefault(type, 0L);
        long previousCount = previousCounts.getOrDefault(type, 0L);

        long increment = currentCount - previousCount;

        StatisticComponent.StatisticResult statisticResult = statisticComponent.calculateRate(currentCount, previousCount);

        return adminMapper.toMetricInfo(
                currentCount,
                increment,
                statisticComponent.roundToFirstDecimal(statisticResult.rate()),
                statisticResult.status()
        );
    }
}
