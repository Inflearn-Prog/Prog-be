package com.progbe.domain.notice.dto;

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
}