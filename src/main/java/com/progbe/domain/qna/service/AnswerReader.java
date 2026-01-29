package com.progbe.domain.qna.service;

import com.progbe.domain.qna.entity.AnswerEntity;
import com.progbe.domain.qna.repository.AnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnswerReader {

    private final AnswerRepository answerRepository;

    public AnswerEntity readByQuestionId(Long questionId) {
        return answerRepository.findByQuestionId(questionId).orElse(null);
    }
}
