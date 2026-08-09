package com.progbe.global.logging;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

import static net.logstash.logback.argument.StructuredArguments.entries;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceMetricsLogger {

    private final MeterRegistry meterRegistry;

    @Scheduled(fixedRateString = "${logging.metrics.interval-ms:30000}")
    public void logResourceMetrics() {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("metricType", "resource");
        entry.put("tomcatThreadsBusy", gaugeValue("tomcat.threads.busy"));
        entry.put("tomcatThreadsMax", gaugeValue("tomcat.threads.config.max"));
        entry.put("hikariActive", gaugeValue("hikaricp.connections.active"));
        entry.put("hikariIdle", gaugeValue("hikaricp.connections.idle"));
        entry.put("hikariPending", gaugeValue("hikaricp.connections.pending"));
        entry.put("hikariMax", gaugeValue("hikaricp.connections.max"));

        log.info("[RESOURCE]", entries(entry));
    }

    private Double gaugeValue(String name) {
        var gauge = meterRegistry.find(name).gauge();
        return gauge != null ? gauge.value() : null;
    }
}
