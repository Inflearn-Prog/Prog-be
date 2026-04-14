package com.progbe.domain.qna.service;

import com.progbe.domain.qna.entity.QuestionEntity;
import com.progbe.domain.qna.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestionDeleter {

    private final QuestionRepository questionRepository;

    public void delete(QuestionEntity question) {
        question.delete();
    }
}
