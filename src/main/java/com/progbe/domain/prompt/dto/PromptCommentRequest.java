package com.progbe.domain.prompt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PromptCommentRequest(
        @NotBlank(message = "댓글 내용은 비어 있을 수 없습니다.")
        @Size(max = 255, message = "댓글은 255자 이하여야 합니다.")
        String comment
) {
}
