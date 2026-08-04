package com.progbe.domain.prompt.dto;

import com.progbe.global.validation.ByteLength;
import com.progbe.global.validation.PlainTextLength;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PromptCreateRequest(
        @NotNull(message = "카테고리는 필수 선택 항목입니다.")
        Long categoryId,

        @NotBlank(message = "제목은 필수 입력 항목입니다.")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
        String title,

        @NotBlank(message = "내용은 필수 입력 항목입니다.")
        @PlainTextLength(max = 5000, message = "내용은 5000자 이하여야 합니다.")
        @ByteLength(max = 60000, message = "이미지나 서식이 너무 많습니다. 이미지를 빼거나, 붙여넣을 때 Ctrl+Shift+V(서식 없이 붙여넣기)를 사용해 주세요.")
        String content
) {
}

