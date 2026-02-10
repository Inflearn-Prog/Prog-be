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

        public StatsSummaryRequest(LocalDate startDate, LocalDate endDate) {
            LocalDate today = LocalDate.now();

            this.startDate = (startDate != null) ? startDate : today;
            this.endDate = (endDate != null) ? endDate : today;

            validate();
        }

        private void validate() {
            if (startDate.isAfter(endDate)) {
                throw new CustomException(ErrorCode.INVALID_DATE_RANGE);
            }

            if (startDate.isAfter(LocalDate.now()) || endDate.isAfter(LocalDate.now())) {
                throw new CustomException(ErrorCode.FUTURE_DATE_NOT_ALLOWED);
            }

            long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
            if (daysBetween > MAX_DATE_RANGE_DAYS) {
                throw new CustomException(ErrorCode.DATE_RANGE_EXCEEDED);
            }
        }

        @Override
        public LocalDate startDate() {
            return startDate;
        }

        @Override
        public LocalDate endDate() {
            return endDate;
        }
    }
}