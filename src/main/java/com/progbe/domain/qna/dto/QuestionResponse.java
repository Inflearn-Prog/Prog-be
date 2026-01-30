package com.progbe.domain.qna.dto;

import com.progbe.domain.qna.entity.QuestionEntity;

import java.util.List;
import java.util.stream.Collectors;

public record QuestionResponse(
        String title,
        String content
) {
    public static QuestionResponse of (QuestionEntity questionEntity) {
        return new QuestionResponse(questionEntity.getTitle(), questionEntity.getContent());
    }

    public static List<QuestionResponse> listOf (List<QuestionEntity> questionEntities) {
        return questionEntities.stream()
                .map(QuestionResponse::of)
                .collect(Collectors.toList());
    }
}
