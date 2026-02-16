package com.progbe.domain.admin.dto;

import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Getter
public class AdminRequest {

    public record StatsSummaryRequest(@DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                      @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        private static final int MAX_DATE_RANGE_DAYS = 30;

        public StatsSummaryRequest {
            LocalDate today = LocalDate.now();

            startDate = (startDate != null) ? startDate : today;
            endDate = (endDate != null) ? endDate : today;

            validate();
        }

        private void validate() {
            LocalDate today = LocalDate.now();

            if (startDate.isAfter(endDate)) {
                throw new CustomException(ErrorCode.INVALID_DATE_RANGE);
            }

            if (startDate.isAfter(today) || endDate.isAfter(today)) {
                throw new CustomException(ErrorCode.FUTURE_DATE_NOT_ALLOWED);
            }

            long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
            if (daysBetween > MAX_DATE_RANGE_DAYS) {
                throw new CustomException(ErrorCode.DATE_RANGE_EXCEEDED);
            }
        }
    }
}