package com.progbe.domain.qna.dto;

import com.progbe.domain.qna.entity.AnswerEntity;
import com.progbe.domain.qna.entity.QuestionEntity;

public record QnAResponse(
        QuestionResponse questionResponse,
        AnswerResponse answerResponse
) {
    public static QnAResponse of(QuestionEntity questionEntity, AnswerEntity answerEntity) {
        return new QnAResponse(
                QuestionResponse.of(questionEntity),

                // QnA 에서는 질문은 무조건 있지만 답변은 무조건 있다고 보장 못하여 null 처리
                (answerEntity == null) ? null : AnswerResponse.of(answerEntity)
        );
    }
}
