package com.progbe.global.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.entries;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
public class LoggingFilter implements Filter {

    private static final long SLOW_THRESHOLD_MS = 2000L;
    private static final double SAMPLING_RATE = 0.1;
    private static final int MAX_BODY_LENGTH = 2000;

    private static final Set<String> EXCLUDED_PATHS = Set.of(
            "/actuator/health", "/actuator/info", "/actuator/metrics", "/ping", "/health"
    );
    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password", "token", "accesstoken", "refreshtoken", "authorization", "secret", "credential"
    );

    private final ObjectMapper objectMapper;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (isExcluded(httpRequest.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper((HttpServletResponse) response);

        Instant start = Instant.now();
        try {
            chain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long durationMs = Duration.between(start, Instant.now()).toMillis();
            int status = wrappedResponse.getStatus();
            boolean isFailure = status >= 400;
            boolean isSlow = durationMs >= SLOW_THRESHOLD_MS;

            if (isFailure || isSlow || shouldSample()) {
                writeLog(wrappedRequest, wrappedResponse, status, durationMs, isFailure, isSlow);
            }

            wrappedResponse.copyBodyToResponse();
        }
    }

    private void writeLog(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response,
                          int status, long durationMs, boolean isFailure, boolean isSlow) {
        try {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("traceId", MDC.get(TraceIdFilter.TRACE_ID));
            entry.put("method", request.getMethod());
            entry.put("uri", request.getRequestURI());
            entry.put("query", maskQueryString(request.getQueryString())); // 쿼리 마스킹
            entry.put("status", status);
            entry.put("durationMs", durationMs);
            entry.put("isSlow", isSlow);
            entry.put("clientIp", resolveClientIp(request));
            entry.put("requestBody", maskBody(request.getContentAsByteArray()));
            Object maskedResponse = maskBody(response.getContentAsByteArray());
            String responseBodyStr = maskedResponse instanceof Map
                    ? objectMapper.writeValueAsString(maskedResponse)
                    : String.valueOf(maskedResponse);
            entry.put("responseBody", truncate(responseBodyStr, MAX_BODY_LENGTH));

            // entries()로 Map 필드들을 Elasticsearch 루트 레벨에 직접 저장
            // (log.info("API {}", json) 방식은 message 문자열 안에 묻혀 Kibana에서 필드 쿼리 불가)
            if (isFailure) {
                log.error("API", entries(entry));
            } else {
                log.info("API", entries(entry));
            }
        } catch (Exception e) {
            log.warn("Failed to write API log", e);
        }
    }

    private Object maskBody(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.readValue(bytes, Map.class);
            maskMap(map);
            return map;  // Map 그대로 반환 → Elasticsearch에 nested object로 저장
        } catch (Exception e) {
            return "[non-JSON body]";
        }
    }

    @SuppressWarnings("unchecked")
    private void maskMap(Map<String, Object> map) {
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (SENSITIVE_FIELDS.contains(e.getKey().toLowerCase())) {
                e.setValue("***");
            } else if (e.getValue() instanceof Map) {
                maskMap((Map<String, Object>) e.getValue());
            } else if (e.getValue() instanceof List) {
                maskList((List<Object>) e.getValue());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void maskList(List<Object> list) {
        for (Object item : list) {
            if (item instanceof Map) {
                maskMap((Map<String, Object>) item);
            } else if (item instanceof List) {
                maskList((List<Object>) item);
            }
        }
    }

    private String maskQueryString(String query) {
        if (query == null || query.isBlank()) return query;
        return Arrays.stream(query.split("&"))
                .map(param -> {
                    int eq = param.indexOf('=');
                    if (eq < 0) return param;
                    String key = param.substring(0, eq);
                    return SENSITIVE_FIELDS.contains(key.toLowerCase()) ? key + "=***" : param;
                })
                .collect(Collectors.joining("&"));
    }

    private boolean isExcluded(String uri) {
        return EXCLUDED_PATHS.stream().anyMatch(uri::startsWith);
    }

    private boolean shouldSample() {
        return Math.random() < SAMPLING_RATE;
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...[truncated]";
    }

    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
        return ip != null ? ip.split(",")[0].trim() : null;
    }
}
