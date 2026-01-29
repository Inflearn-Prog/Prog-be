package com.progbe.domain.qna.service;

import com.progbe.domain.qna.dto.*;
import com.progbe.domain.qna.entity.AnswerEntity;
import com.progbe.domain.qna.entity.QuestionEntity;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.domain.user.service.UserService;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QnAService {

    private final QuestionCreator questionCreator;
    private final QuestionReader questionReader;
    private final QuestionDeleter questionDeleter;

    private final AnswerCreator answerCreator;
    private final AnswerReader answerReader;
    private final AnswerDeleter answerDeleter;

    private final QnAValidator qnaValidator;

    private final UserService userService;

    // 질문 생성
    public QuestionResponse createQuestion(Long userId, QuestionRequest questionRequest) {
        UserEntity user = userService.getUserById(userId);

        QuestionEntity question = questionCreator.createQuestion(user, questionRequest);

        return QuestionResponse.of(question);
    }

    // 응답 생성
    public AnswerResponse createAnswer(Long userId, Long questionId, AnswerRequest answerRequest) {
        if (!userService.checkAdmin(userId)) {
            throw new CustomException(ErrorCode.NOT_ADMIN);
        }

        if (qnaValidator.checkExistAnswer(questionId)) {
            throw new CustomException(ErrorCode.ANSWER_ALREADY_EXISTS);
        }

        UserEntity user = userService.getUserById(userId);

        QuestionEntity questionEntity = questionReader.readByQuestionId(questionId);

        AnswerEntity answer = answerCreator.createAnswer(user, questionEntity, answerRequest);

        return AnswerResponse.of(answer);
    }

    // 다건 조회
    //TODO : UI에 맞게 페이징 및 슬라이싱 처리
    public List<QuestionResponse> getQuestions() {
        List<QuestionEntity> questions = questionReader.readAll();
        return QuestionResponse.listOf(questions);
    }

    // 단건 조회
    public QnAResponse getQnAById(Long questionId) {
        QuestionEntity questionEntity = questionReader.readByQuestionId(questionId);

        AnswerEntity answerEntity = answerReader.readByQuestionId(questionId);

        return QnAResponse.of(questionEntity, answerEntity);
    }

    // 문의 내역 삭제
    @Transactional
    public void deleteQnAById(Long userId, Long questionId) {

        QuestionEntity question = questionReader.readByQuestionId(questionId);

        if (qnaValidator.checkWriter(userId, questionId)) {
            answerDeleter.deleteAnswer(questionId);

            questionDeleter.delete(question);

        } else {
            throw new CustomException(ErrorCode.NOT_QUESTION_WRITER);
        }
    }
}
