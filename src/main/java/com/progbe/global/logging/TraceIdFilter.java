package com.progbe.global.logging;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter implements Filter {

    static final String TRACE_ID = "traceId";
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String traceId = UUID.randomUUID().toString().replace("-", "");
        try {
            MDC.put(TRACE_ID, traceId);
            ((HttpServletResponse) response).setHeader(TRACE_ID_HEADER, traceId);
            chain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID);
            // JwtAuthenticationFilter가 심어놓은 userId 정리.
            // JwtAuthenticationFilter 자체에서 지우면 이 필터(가장 바깥쪽, LoggingFilter를 감쌈)보다
            // 먼저 지워져서 LoggingFilter가 로그를 쓸 때 이미 사라진 상태가 됨 -> 따러서 여기서 정리
            MDC.remove("userId");
        }
    }
}
