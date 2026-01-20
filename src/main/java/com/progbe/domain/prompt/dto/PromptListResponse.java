package com.progbe.domain.prompt.dto;

import java.util.List;

public record PromptListResponse(
        List<PromptSummaryResponse> prompts,
        long totalCount
) {
}

