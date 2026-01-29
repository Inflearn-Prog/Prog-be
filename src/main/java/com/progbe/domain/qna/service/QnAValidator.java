package com.progbe.domain.qna.service;

import com.progbe.domain.qna.entity.AnswerEntity;
import com.progbe.domain.qna.entity.QuestionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QnAValidator {

    private final QuestionReader questionReader;
    private final AnswerReader answerReader;

    // 본인이 작성한 문의 글인지
    public boolean checkWriter(Long userId, Long questionId) {
        QuestionEntity questionEntity = questionReader.readByQuestionId(questionId);

        Long userIdByQuestionId = questionEntity.getUser().getId();

        return userId.equals(userIdByQuestionId);
    }

    // 이미 답변이 존재하는지
    // TODO : 엔티티 조회 대신 카운트 조회로도 확인할 수 있는 방법이 있다
    public boolean checkExistAnswer(Long questionId) {
        AnswerEntity answer = answerReader.readByQuestionId(questionId);

        return answer != null;
    }

}
