package com.progbe.domain.user.entity;

import com.progbe.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_withdrawal_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserWithdrawalHistoryEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_withdrawal_history_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Builder
    public UserWithdrawalHistoryEntity(Long userId, String reason) {
        this.userId = userId;
        this.reason = reason;
    }
}