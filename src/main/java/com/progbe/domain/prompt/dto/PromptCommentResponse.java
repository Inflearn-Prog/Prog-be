package com.progbe.domain.prompt.dto;

import com.progbe.domain.prompt.entity.PromptCommentEntity;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;

public record PromptCommentResponse(
        Long commentId,
        String nickName,
        String comment,
        Long parentId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PromptCommentResponse of(PromptCommentEntity promptCommentEntity) {
        return new PromptCommentResponse(
                promptCommentEntity.getId(),
                promptCommentEntity.getUser().getNickname(),
                promptCommentEntity.getComment(),
                promptCommentEntity.getParentId(),
                promptCommentEntity.getCreatedAt(),
                promptCommentEntity.getUpdatedAt()
        );
    }

    public static List<PromptCommentResponse> listOf(List<PromptCommentEntity> commentEntityList) {
        return commentEntityList.stream()
                .map(PromptCommentResponse::of)
                .toList();
    }

    public static Slice<PromptCommentResponse> sliceOf(Slice<PromptCommentEntity> commentEntitySlice) {
        return commentEntitySlice.map(PromptCommentResponse::of);
    }
}
