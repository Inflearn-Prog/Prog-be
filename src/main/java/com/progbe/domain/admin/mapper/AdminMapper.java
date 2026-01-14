package com.progbe.domain.admin.mapper;

import com.progbe.domain.admin.dto.AdminResponse;
import com.progbe.domain.admin.entity.StatisticType;
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
}
