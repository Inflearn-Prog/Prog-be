package com.progbe.domain.qna.service;

import com.progbe.domain.qna.entity.QuestionEntity;
import com.progbe.domain.qna.repository.QuestionRepository;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class QuestionReader {

    private final QuestionRepository questionRepository;

    public QuestionEntity readByQuestionId(Long questionId) {
        return questionRepository.findById(questionId).orElseThrow(
                () -> new CustomException(ErrorCode.QUESTION_NOT_FOUND)
        );
    }

    public List<QuestionEntity> readAll() {
        return questionRepository.findAll();
    }
}
