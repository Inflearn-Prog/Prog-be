package com.progbe.domain.prompt.dto;

import com.progbe.global.validation.PlainTextLength;
import jakarta.validation.constraints.Size;

public record PromptUpdateRequest(
        Long categoryId,

        @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
        String title,

        @PlainTextLength(max = 5000, message = "내용은 5000자 이하여야 합니다.")
        @Size(max = 20000, message = "내용의 서식 데이터가 너무 큽니다.")
        String content
) {
}

