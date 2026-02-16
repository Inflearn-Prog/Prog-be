package com.progbe.domain.admin.dto;

import com.progbe.domain.user.type.Role;
import com.progbe.domain.user.type.UserStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class AdminUserRequest {

    public record BulkStatusUpdateRequest(
            @NotNull(message = "유저 ID 목록은 필수입니다.")
            @Size(min = 1, max = 100, message = "유저 ID는 1개 이상 100개 이하여야 합니다.")
            List<Long> userIds,

            @NotNull(message = "변경할 상태는 필수입니다.")
            UserStatus status
    ) {
    }

    public record BulkRoleUpdateRequest(
            @NotNull(message = "유저 ID 목록은 필수입니다.")
            @Size(min = 1, max = 100, message = "유저 ID는 1개 이상 100개 이하여야 합니다.")
            List<Long> userIds,

            @NotNull(message = "변경할 권한은 필수입니다.")
            Role role
    ) {
    }
}