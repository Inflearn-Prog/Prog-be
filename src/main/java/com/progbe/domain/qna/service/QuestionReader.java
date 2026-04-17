package com.progbe.domain.qna.service;

import com.progbe.domain.qna.entity.QuestionEntity;
import com.progbe.domain.qna.repository.QuestionRepository;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestionReader {

    private final QuestionRepository questionRepository;

    public QuestionEntity readByQuestionId(Long questionId) {
        return questionRepository.findByIdAndNotDeleted(questionId).orElseThrow(
                () -> new CustomException(ErrorCode.QUESTION_NOT_FOUND)
        );
    }

    public Page<QuestionEntity> readAll(Pageable pageable) {
        return questionRepository.findAllByNotDeleted(pageable);
    }
}
