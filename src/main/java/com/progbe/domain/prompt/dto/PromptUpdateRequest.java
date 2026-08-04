package com.progbe.domain.prompt.dto;

import com.progbe.global.validation.ByteLength;
import com.progbe.global.validation.PlainTextLength;
import jakarta.validation.constraints.Size;

public record PromptUpdateRequest(
        Long categoryId,

        @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
        String title,

        @PlainTextLength(max = 5000, message = "내용은 5000자 이하여야 합니다.")
        @ByteLength(max = 60000, message = "이미지나 서식이 너무 많습니다. 이미지를 빼거나, 붙여넣을 때 Ctrl+Shift+V(서식 없이 붙여넣기)를 사용해 주세요.")
        String content
) {
}

