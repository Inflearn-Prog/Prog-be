package com.progbe.domain.auth.entity;

import com.progbe.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "refresh_tokens",
        indexes = {
                @Index(name = "idx_user_id", columnList = "user_id"),
                @Index(name = "idx_token_value", columnList = "token_value"),
                @Index(name = "idx_user_device_revoked", columnList = "user_id, device_info, is_revoked") // [Refactor] 복합 인덱스 추가
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshTokenEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_token_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token_value", nullable = false, unique = true, length = 500)
    private String tokenValue;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_revoked", nullable = false)
    private boolean isRevoked = false;

    @Column(name = "device_info")
    private String deviceInfo;

    @Builder
    public RefreshTokenEntity(Long userId, String tokenValue, LocalDateTime expiresAt, String deviceInfo) {
        this.userId = userId;
        this.tokenValue = tokenValue;
        this.expiresAt = expiresAt;
        this.deviceInfo = deviceInfo;
        this.isRevoked = false;
    }

    public void revoke() {
        this.isRevoked = true;
    }

    public boolean isCurrentlyExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !isRevoked && !isCurrentlyExpired();
    }
}