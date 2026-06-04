package com.progbe.domain.admin.dto;

import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AdminRequest.StatsSummaryRequest 생성/검증")
class AdminRequestTest {

    @Nested
    @DisplayName("기본값 처리 (회귀: validate() NPE → 500)")
    class Defaulting {

        @Test
        @DisplayName("startDate/endDate가 모두 null이면 NPE 없이 오늘 날짜로 채워진다")
        void nullDates_defaultToToday() {
            LocalDate today = LocalDate.now();

            AdminRequest.StatsSummaryRequest request =
                    new AdminRequest.StatsSummaryRequest(null, null);

            assertThat(request.startDate()).isEqualTo(today);
            assertThat(request.endDate()).isEqualTo(today);
        }

        @Test
        @DisplayName("null 입력 시 예외가 전혀 발생하지 않는다")
        void nullDates_doNotThrow() {
            assertThatCode(() -> new AdminRequest.StatsSummaryRequest(null, null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("startDate만 null이면 endDate는 유지되고 startDate만 오늘로 채워진다")
        void onlyStartNull_defaultsStart() {
            LocalDate today = LocalDate.now();

            AdminRequest.StatsSummaryRequest request =
                    new AdminRequest.StatsSummaryRequest(null, today);

            assertThat(request.startDate()).isEqualTo(today);
            assertThat(request.endDate()).isEqualTo(today);
        }

        @Test
        @DisplayName("유효한 두 날짜가 주어지면 그대로 보존된다")
        void validDates_preserved() {
            LocalDate start = LocalDate.now().minusDays(7);
            LocalDate end = LocalDate.now().minusDays(1);

            AdminRequest.StatsSummaryRequest request =
                    new AdminRequest.StatsSummaryRequest(start, end);

            assertThat(request.startDate()).isEqualTo(start);
            assertThat(request.endDate()).isEqualTo(end);
        }
    }

    @Nested
    @DisplayName("검증 로직 (NPE 제거 후에도 validate가 살아있는지)")
    class Validation {

        @Test
        @DisplayName("startDate가 endDate보다 뒤면 INVALID_DATE_RANGE")
        void startAfterEnd_throws() {
            LocalDate start = LocalDate.now().minusDays(1);
            LocalDate end = LocalDate.now().minusDays(5);

            assertThatThrownBy(() -> new AdminRequest.StatsSummaryRequest(start, end))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_DATE_RANGE);
        }

        @Test
        @DisplayName("미래 날짜는 FUTURE_DATE_NOT_ALLOWED")
        void futureDate_throws() {
            LocalDate future = LocalDate.now().plusDays(1);

            assertThatThrownBy(() -> new AdminRequest.StatsSummaryRequest(future, future))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.FUTURE_DATE_NOT_ALLOWED);
        }

        @Test
        @DisplayName("조회 기간이 30일을 초과하면 DATE_RANGE_EXCEEDED")
        void rangeExceeded_throws() {
            LocalDate start = LocalDate.now().minusDays(31);
            LocalDate end = LocalDate.now();

            assertThatThrownBy(() -> new AdminRequest.StatsSummaryRequest(start, end))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.DATE_RANGE_EXCEEDED);
        }
    }
}
