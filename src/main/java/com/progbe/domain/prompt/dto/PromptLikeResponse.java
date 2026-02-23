package com.progbe.domain.prompt.dto;

public record PromptLikeResponse(
    String likeStatus
) {
    public static PromptLikeResponse of(LikeStatus likeStatus) {
        return new PromptLikeResponse(likeStatus.name());
    }
}
