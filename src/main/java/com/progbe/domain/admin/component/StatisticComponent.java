package com.progbe.domain.admin.component;

import com.progbe.domain.admin.type.MetricCalculationStatus;
import org.springframework.stereotype.Component;

@Component
public class StatisticComponent {

    public record StatisticResult(double rate, MetricCalculationStatus status) {
    }

    public StatisticResult calculateRate(long current, long previous) {
        if (current < 0 || previous < 0) {
            return new StatisticResult(0, MetricCalculationStatus.INVALID_INPUT);
        }

        if (previous == 0) {
            double rate = current > 0 ? 100 : 0;
            return new StatisticResult(rate, MetricCalculationStatus.ZERO_PREVIOUS);
        }

        double rate = ((double) (current - previous) / previous) * 100;
        return new StatisticResult(rate, MetricCalculationStatus.SUCCESS);
    }

    public double roundToFirstDecimal(double value) {
        return Math.round(value * 10) / 10.0;
    }
}