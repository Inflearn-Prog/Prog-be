package com.progbe.domain.qna.dto;

import com.progbe.domain.qna.entity.AnswerEntity;

public record AnswerResponse(
        Long answerId,
        String content
) {
    public static AnswerResponse of(AnswerEntity answerEntity) {
        return new AnswerResponse(
                answerEntity.getId(),
                answerEntity.getContent());
    }
}
