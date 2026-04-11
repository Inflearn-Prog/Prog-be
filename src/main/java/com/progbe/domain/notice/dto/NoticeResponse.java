package com.progbe.domain.notice.dto;

import com.progbe.global.common.CommonResponse;

import java.time.LocalDateTime;
import java.util.List;

public class NoticeResponse {

    public record CreateNoticeResponse(
            Long noticeId,
            String message
    ) {
    }

    public record BulkDeleteResponse(
            int deletedCount,
            String message
    ) {
    }

    public record UpdateNoticeResponse(
            Long noticeId,
            String message
    ) {
    }

    public record NoticeListResponse(
            List<NoticeInfo> content,
            CommonResponse.PageInfoResponse pageInfo
    ) {
    }

    public record NoticeInfo(
            Long noticeId,
            String title,
            LocalDateTime createdAt
    ) {
    }

    public record NoticeDetailResponse(
            Long noticeId,
            String title,
            String content,
            LocalDateTime createdAt
    ) {
    }
}