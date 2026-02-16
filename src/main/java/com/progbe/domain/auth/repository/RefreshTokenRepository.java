package com.progbe.domain.auth.repository;

import com.progbe.domain.auth.entity.RefreshTokenEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByTokenValue(String tokenValue);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT rt FROM RefreshTokenEntity rt WHERE rt.tokenValue = :tokenValue")
    Optional<RefreshTokenEntity> findByTokenValueForUpdate(@Param("tokenValue") String tokenValue);

    List<RefreshTokenEntity> findByUserId(Long userId);

    @Modifying
    @Query("UPDATE RefreshTokenEntity rt SET rt.isRevoked = true WHERE rt.userId = :userId")
    void revokeAllByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE RefreshTokenEntity rt SET rt.isRevoked = true WHERE rt.tokenValue = :tokenValue")
    void revokeByTokenValue(@Param("tokenValue") String tokenValue);

    @Modifying
    @Query("DELETE FROM RefreshTokenEntity rt WHERE rt.expiresAt < :now OR rt.isRevoked = true")
    void deleteExpiredAndRevokedTokens(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE RefreshTokenEntity rt SET rt.isRevoked = true WHERE rt.userId = :userId AND rt.deviceInfo = :deviceInfo AND rt.isRevoked = false")
    void revokeByUserIdAndDeviceInfo(@Param("userId") Long userId, @Param("deviceInfo") String deviceInfo);
}