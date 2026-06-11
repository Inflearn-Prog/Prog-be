package com.progbe.global.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.aspectj.lang.annotation.Aspect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import static org.assertj.core.api.Assertions.assertThat;

class ExecutionTimeAspectTest {

    private ListAppender<ILoggingEvent> listAppender;
    private Logger aspectLogger;

    @BeforeEach
    void setUp() {
        aspectLogger = (Logger) LoggerFactory.getLogger(ExecutionTimeAspect.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        aspectLogger.addAppender(listAppender);
        aspectLogger.setLevel(Level.DEBUG);
    }

    @AfterEach
    void tearDown() {
        aspectLogger.detachAppender(listAppender);
    }

    private SampleService createProxiedService() {
        SampleService target = new SampleService();
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(new ExecutionTimeAspect());
        return factory.getProxy();
    }

    @Test
    @DisplayName("정상 속도 메서드는 INFO 레벨로 [PERF] 로그를 남긴다")
    void logsInfoWhenUnderThreshold() {
        SampleService service = createProxiedService();

        service.fastMethod();

        assertThat(listAppender.list)
                .anyMatch(e -> e.getLevel() == Level.INFO
                        && e.getFormattedMessage().contains("[PERF]")
                        && e.getFormattedMessage().contains("fastMethod"));
    }

    @Test
    @DisplayName("임계값 초과 메서드는 WARN 레벨로 [PERF] 로그를 남긴다")
    void logsWarnWhenOverThreshold() {
        SampleService service = createProxiedService();

        service.slowMethod();

        assertThat(listAppender.list)
                .anyMatch(e -> e.getLevel() == Level.WARN
                        && e.getFormattedMessage().contains("[PERF]")
                        && e.getFormattedMessage().contains("slowMethod"));
    }

    @Test
    @DisplayName("로그 메시지에 실행 시간(ms)이 포함된다")
    void logMessageContainsElapsedTime() {
        SampleService service = createProxiedService();

        service.fastMethod();

        String message = listAppender.list.stream()
                .filter(e -> e.getFormattedMessage().contains("fastMethod"))
                .findFirst()
                .map(ILoggingEvent::getFormattedMessage)
                .orElse("");

        assertThat(message).contains("ms");
    }

    // 테스트용 더미 서비스
    static class SampleService {

        @MeasureExecutionTime(warnThresholdMs = 100)
        public void fastMethod() {
            // 즉시 반환 — 100ms 임계값 미만
        }

        @MeasureExecutionTime(warnThresholdMs = 1)
        public void slowMethod() {
            try {
                Thread.sleep(10); // 1ms 임계값 초과
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
