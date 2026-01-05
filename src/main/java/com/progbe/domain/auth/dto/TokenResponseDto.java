package com.progbe.domain.auth.dto;

public record TokenResponseDto(
        String accessToken,
        String refreshToken
) {
}