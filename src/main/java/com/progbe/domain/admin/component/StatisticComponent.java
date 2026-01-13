package com.progbe.domain.admin.component;


import org.springframework.stereotype.Component;

@Component
public class StatisticComponent {


    public double calculateRate(long today, long yesterday) {

        // 어제 데이터가 없을 때
        if (yesterday == 0) {
            return today > 0 ? 100.0 : 0.0;
        }

        return ((double) (today - yesterday) / yesterday) * 100;
    }
}
