package com.progbe.domain.admin.dto;

import com.progbe.domain.prompt.type.PromptStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class AdminPromptRequest {

    public record BulkUpdateRequest(
            @NotNull(message = "프롬프트 ID 목록은 필수입니다.")
            @Size(min = 1, max = 100, message = "프롬프트 ID는 1개 이상 100개 이하여야 합니다.")
            List<Long> promptIds,

            @NotNull(message = "업데이트 필드는 필수입니다.")
            UpdateFields updateFields
    ) {
    }

    public record UpdateFields(
            Long categoryId,
            PromptStatus status
    ) {
    }

    public record BulkDeleteRequest(
            @NotNull(message = "프롬프트 ID 목록은 필수입니다.")
            @Size(min = 1, max = 100, message = "프롬프트 ID는 1개 이상 100개 이하여야 합니다.")
            List<Long> promptIds
    ) {
    }
}
