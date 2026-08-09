package com.progbe.global.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 인증 성공 시 MDC에 userId 추가 -> 유니크 사용자 수 집계 위함 (TODO: 추후 Redis SCARD로 실시간 동시 접속자 수 구하기..)
            // MDC.remove는 여기서 하지 않음 — 이 필터를 감싸는 LoggingFilter가 로그를 쓸 때까지 값이 살아있어야 함.
            // 실제 정리는 가장 바깥쪽 TraceIdFilter의 finally에서 traceId와 함께 처리.
            MDC.put("userId", authentication.getName());
        }

        filterChain.doFilter(request, response);
    }
}
