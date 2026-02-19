package com.progbe.domain.auth.scheduler;

import com.progbe.domain.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime revokedThreshold = now.minusDays(30); // TODO : 구체적인 시간은 협의 후 재적용
        
        log.info("만료된 Refresh Token 정리 시작 (revoked 토큰은 30일 보관)");
        refreshTokenRepository.deleteExpiredAndRevokedTokens(now, revokedThreshold);
        log.info("만료된 Refresh Token 정리 완료");
    }
}