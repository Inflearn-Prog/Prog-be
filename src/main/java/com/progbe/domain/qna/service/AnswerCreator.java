package com.progbe.domain.qna.service;

import com.progbe.domain.qna.dto.AnswerRequest;
import com.progbe.domain.qna.entity.AnswerEntity;
import com.progbe.domain.qna.entity.QuestionEntity;
import com.progbe.domain.qna.repository.AnswerRepository;
import com.progbe.domain.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AnswerCreator {

    private final AnswerRepository answerRepository;

    // 응답 생성
    @Transactional
    public AnswerEntity createAnswer(UserEntity user, QuestionEntity questionEntity, AnswerRequest answerRequest) {
        AnswerEntity answerEntity = AnswerEntity.from(user, questionEntity, answerRequest);
        return answerRepository.save(answerEntity);
    }
}
