package com.progbe.domain.user.dto;

import java.time.LocalDateTime;

public record UserWithdrawalResponse(
        String uid,
        String unlinkedProvider,
        LocalDateTime terminatedAt
) {
}
