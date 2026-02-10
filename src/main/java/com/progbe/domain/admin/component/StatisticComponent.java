package com.progbe.domain.admin.component;

import org.springframework.stereotype.Component;

@Component
public class StatisticComponent {

    public double calculateRate(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }

        if (current < 0 || previous < 0) {
            return 0.0;
        }

        return ((double) (current - previous) / previous) * 100;
    }

    public double roundToFirstDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}