package com.progbe.domain.admin.dto;

import com.progbe.domain.user.type.Role;
import com.progbe.domain.user.type.UserStatus;
import com.progbe.global.common.CommonResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class AdminUserResponse {

    @Getter
    @AllArgsConstructor
    @Builder
    public static class UserListResponse {
        private List<UserInfo> content;
        private CommonResponse.PageInfoResponse pageInfo;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private Long userId;
        private String nickname;
        private Role role;
        private long promptCount;
        private long commentCount;
        private UserStatus status;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class BulkUpdateResponse {
        private int updatedCount;
        private String message;
    }
}
