package com.progbe.domain.qna.service;

import com.progbe.domain.qna.entity.AnswerEntity;
import com.progbe.domain.qna.repository.AnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnswerDeleter {

    private final AnswerReader answerReader;

    private final AnswerRepository answerRepository;

    public void deleteAnswer(Long questionId) {

        AnswerEntity answerEntity = answerReader.readByQuestionId(questionId);

        if (answerEntity != null) {
            answerRepository.delete(answerEntity);
        }
    }
}
