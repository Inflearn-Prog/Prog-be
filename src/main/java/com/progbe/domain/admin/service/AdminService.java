package com.progbe.domain.admin.service;

import com.progbe.domain.admin.entity.StatisticEntity;
import com.progbe.domain.admin.entity.StatisticType;
import com.progbe.domain.admin.repository.StatisticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminService
{

    private final StatisticRepository statisticRepository;

    @Transactional
    public void increaseToday(StatisticType type) {

        LocalDateTime today = LocalDate.now().atStartOfDay();

        int updated = statisticRepository.increaseTodayCount(type, today);

        if (updated == 0) {
            // 오늘 row가 없으면 생성
            StatisticEntity statistic = StatisticEntity.builder().count(1).build();
            statisticRepository.save(statistic);
        }
    }
}
