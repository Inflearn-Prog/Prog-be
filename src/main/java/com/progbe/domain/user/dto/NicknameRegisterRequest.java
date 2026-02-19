package com.progbe.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public record NicknameRegisterRequest(
        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname
) {
}
