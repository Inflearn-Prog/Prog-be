package com.progbe.domain.admin.service;

import com.progbe.domain.admin.component.StatisticComponent;
import com.progbe.domain.admin.dto.AdminResponse;
import com.progbe.domain.admin.entity.StatisticEntity;
import com.progbe.domain.admin.entity.StatisticType;
import com.progbe.domain.admin.mapper.AdminMapper;
import com.progbe.domain.admin.repository.StatisticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService
{

    private final StatisticRepository statisticRepository;
    private final StatisticComponent statisticComponent;
    private final List<StatisticType> statisticTypeList;
    private final AdminMapper adminMapper;

    @Transactional
    public void increaseToday(StatisticType type) {

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        int updated = statisticRepository.increaseTodayCount(
                type,
                startOfDay,
                endOfDay
        );

        if (updated == 0) {
            // 오늘 row가 없으면 생성
            StatisticEntity statistic = StatisticEntity.builder().type(type).count(1).build();
            statisticRepository.save(statistic);
        }
    }







    @Transactional(readOnly = true)
    public AdminResponse.DailyStatisticSummaryResponse getDailyStatisticSummary() {

        LocalDateTime today = LocalDate.now().atStartOfDay();
        LocalDateTime yesterday = today.minusDays(1);

        Map<StatisticType, Double> rateMap = new EnumMap<>(StatisticType.class);
        Map<StatisticType, Long> todayCountMap = new EnumMap<>(StatisticType.class);

        for (StatisticType type : statisticTypeList) {

            long todayCount = getCount(type, today);
            long yesterdayCount = getCount(type, yesterday);

            double rate = statisticComponent.calculateRate(todayCount, yesterdayCount);
            rateMap.put(type, rate);
            todayCountMap.put(type,todayCount);
        }
        return adminMapper.toDailyStatisticSummaryResponse(rateMap,todayCountMap);

    }

    private long getCount(StatisticType type, LocalDateTime date) {
        return Optional.of(
                statisticRepository.findTotalByTypeAndCreatedAt(type, date)
        ).orElse(0L);
    }

}
