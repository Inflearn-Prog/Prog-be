package com.progbe.domain.user.dto;

import com.progbe.domain.user.entity.UserEntity;

public record UserLoginResult(
        UserEntity user,
        boolean isNewUser
) {
}