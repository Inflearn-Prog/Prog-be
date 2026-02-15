package com.progbe.domain.admin.dto;

import com.progbe.domain.prompt.type.PromptStatus;
import com.progbe.global.common.CommonResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class AdminPromptResponse {

    @Getter
    @AllArgsConstructor
    @Builder
    public static class PromptListResponse {
        private List<PromptInfo> content;
        private CommonResponse.PageInfoResponse pageInfo;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class PromptInfo {
        private Long promptId;
        private String title;
        private String authorNickname;
        private String categoryName;
        private PromptStatus status;
        private String createdAt;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class BulkUpdateResponse {
        private int updatedCount;
        private String message;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class BulkDeleteResponse {
        private int deletedCount;
        private String message;
    }
}
