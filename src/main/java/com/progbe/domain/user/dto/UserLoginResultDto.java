package com.progbe.domain.user.dto;

import com.progbe.domain.user.entity.UserEntity;

public record UserLoginResultDto(
        UserEntity user,
        boolean isNewUser
) {
}