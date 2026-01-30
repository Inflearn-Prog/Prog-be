package com.progbe.domain.qna.service;

import com.progbe.domain.qna.dto.QuestionRequest;
import com.progbe.domain.qna.entity.QuestionEntity;
import com.progbe.domain.qna.repository.QuestionRepository;
import com.progbe.domain.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestionCreator {

    private final QuestionRepository questionRepository;

    // 질문 생성 로직ø
    public QuestionEntity createQuestion(UserEntity user, QuestionRequest questionRequest) {
        QuestionEntity questionEntity = QuestionEntity.from(user, questionRequest);
        return questionRepository.save(questionEntity);
    }
}
