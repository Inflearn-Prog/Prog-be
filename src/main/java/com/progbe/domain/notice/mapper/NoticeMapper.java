package com.progbe.domain.notice.mapper;

import com.progbe.domain.notice.dto.NoticeRequest;
import com.progbe.domain.notice.dto.NoticeResponse;
import com.progbe.domain.notice.entity.NoticeEntity;
import org.springframework.stereotype.Component;

@Component
public class NoticeMapper {

    public NoticeEntity toEntity(NoticeRequest.CreateNoticeRequest request) {
        return NoticeEntity.builder()
                .title(request.title().trim())
                .content(request.content().trim())
                .build();
    }

    public NoticeResponse.CreateNoticeResponse toCreateResponse(NoticeEntity savedNotice) {
        return new NoticeResponse.CreateNoticeResponse(
                savedNotice.getId(),
                "공지가 성공적으로 등록되었습니다."
        );
    }

    public NoticeResponse.BulkDeleteResponse toBulkDeleteResponse(int requestedCount, int deletedCount) {
        String message = buildDeleteMessage(requestedCount, deletedCount);
        return new NoticeResponse.BulkDeleteResponse(deletedCount, message);
    }

    private String buildDeleteMessage(int requestedCount, int deletedCount) {
        if (deletedCount == requestedCount) {
            return "선택한 공지사항이 삭제되었습니다.";
        }

        if (deletedCount == 0) {
            return String.format(
                    "0개의 공지사항이 삭제되었습니다. (%d개는 이미 삭제되었거나 존재하지 않는 공지사항입니다.)",
                    requestedCount
            );
        }

        int notDeletedCount = requestedCount - deletedCount;
        return String.format(
                "%d개의 공지사항이 삭제되었습니다. (%d개는 이미 삭제되었거나 존재하지 않는 공지사항입니다.)",
                deletedCount,
                notDeletedCount
        );
    }
}