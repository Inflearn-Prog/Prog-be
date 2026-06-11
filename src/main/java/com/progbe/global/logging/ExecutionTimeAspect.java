package com.progbe.global.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExecutionTimeAspect {

    private static final Logger log = LoggerFactory.getLogger(ExecutionTimeAspect.class);

    @Around("@annotation(measureExecutionTime)")
    public Object measure(ProceedingJoinPoint joinPoint, MeasureExecutionTime measureExecutionTime) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long executionTimeMs = System.currentTimeMillis() - start; // 현재 시각 - 시작 시각 => 실행 시간으로 설정
            String methodName = ((MethodSignature) joinPoint.getSignature()).getMethod().getName();
            String traceId = MDC.get(TraceIdFilter.TRACE_ID);
            String message = "[퍼포먼스] {} took {} ms" + (traceId != null ? " (traceId={})" : ""); // 추후, 퍼포먼스 측정 고도화되면 MDC - logType 에 PERF값 넣기로 함

            if (executionTimeMs > measureExecutionTime.warnThresholdMs()) { // 실행시간 > 기준시간일 시 => WARN처리
                if (traceId != null) {
                    log.warn(message, methodName, executionTimeMs, traceId);
                } else {
                    log.warn("[퍼포먼스] {} took {} ms", methodName, executionTimeMs);
                }
            } else {
                if (traceId != null) {
                    log.info("[퍼포먼스] {} took {} ms (traceId={})", methodName, executionTimeMs, traceId);
                } else {
                    log.info("[퍼포먼스] {} took {} ms", methodName, executionTimeMs);
                }
            }
        }
    }
}
