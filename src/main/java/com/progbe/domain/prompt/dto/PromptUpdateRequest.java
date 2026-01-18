package com.progbe.domain.prompt.dto;

import jakarta.validation.constraints.Size;

public record PromptUpdateRequest(
        Long categoryId,

        @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
        String title,

        @Size(max = 5000, message = "내용은 5000자 이하여야 합니다.")
        String content
) {
}

