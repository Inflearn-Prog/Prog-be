package com.progbe.domain.notice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public class NoticeRequest {

    public record CreateNoticeRequest(
            @NotBlank(message = "제목은 필수입니다.")
            @Size(max = 50, message = "제목은 50자 이내여야 합니다.")
            @Pattern(regexp = ".*\\S.*", message = "제목은 공백만으로 구성될 수 없습니다.")
            String title,

            @NotBlank(message = "내용은 필수입니다.")
            @Size(max = 5000, message = "내용은 5000자 이내여야 합니다.")
            @Pattern(regexp = ".*\\S.*", message = "내용은 공백만으로 구성될 수 없습니다.")
            String content
    ) {
    }

    public record UpdateNoticeRequest(
            @Size(max = 50, message = "제목은 50자 이내여야 합니다.")
            String title,

            @Size(max = 5000, message = "내용은 5000자 이내여야 합니다.")
            String content
    ) {
    }

    public record BulkDeleteRequest(
            @NotNull(message = "삭제할 공지사항 ID 목록은 필수입니다.")
            @Size(min = 1, message = "최소 1개 이상의 ID가 필요합니다.")
            @Size(max = 100, message = "한 번에 최대 100개까지 삭제할 수 있습니다.")
            List<Long> noticeIds
    ) {
    }
}