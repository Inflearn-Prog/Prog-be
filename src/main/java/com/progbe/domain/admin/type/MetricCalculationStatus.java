package com.progbe.domain.admin.type;

/*
 * 계산 상태
 * SUCCESS : 정상 계산
 * ZERO_PREVIOUS : 분모가 0이라 계산 불가
 * INVALID_INPUT : 유효하지 않은 값 (음수 입력)
 */
public enum MetricCalculationStatus {
    SUCCESS,
    ZERO_PREVIOUS,
    INVALID_INPUT
}