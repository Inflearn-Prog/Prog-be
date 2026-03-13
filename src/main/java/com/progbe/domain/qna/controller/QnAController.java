package com.progbe.domain.qna.controller;

import com.progbe.domain.qna.dto.*;
import com.progbe.domain.qna.service.QnAService;
import com.progbe.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/qna")
@RequiredArgsConstructor
@Slf4j
public class QnAController {

    private final QnAService qnAService;

    // 문의하기
    @PostMapping
    public ApiResponse<QuestionResponse> createQuestion(@AuthenticationPrincipal UserDetails userDetails,
                                                        @RequestBody QuestionRequest questionRequest) {
        log.info("[문의 생성 API] : 요청");
        Long userId = Long.parseLong(userDetails.getUsername());
        QuestionResponse result = qnAService.createQuestion(userId, questionRequest);
        log.info("[문의 생성 API] : 완료");
        return ApiResponse.success(result);
    }

    // 답변하기
    @PostMapping("/{questionId}")
    public ApiResponse<AnswerResponse> createAnswer(@AuthenticationPrincipal UserDetails userDetails,
                                                      @PathVariable Long questionId,
                                                      @RequestBody AnswerRequest answerRequest) {
        log.info("[문의 응답 API] : 요청");
        Long userId = Long.parseLong(userDetails.getUsername());
        AnswerResponse result = qnAService.createAnswer(userId, questionId, answerRequest);
        log.info("[문의 응답 API] : 완료");
        return ApiResponse.success(result);
    }

    // 전체 문의 내역 보기
    @GetMapping("/list")
    public ApiResponse<List<QuestionResponse>> list() {
        log.info("[문의 전체 확인 API] : 요청");
        List<QuestionResponse> result = qnAService.getQuestions();
        log.info("[문의 전체 확인 API] : 완료");
        return ApiResponse.success(result);
    }

    // 문의 답변 및 내역 보기
    @GetMapping("/{questionId}")
    public ApiResponse<QnAResponse> getQuestion(@PathVariable Long questionId) {
        log.info("[문의 단건 확인 API] : 요청");
        QnAResponse result = qnAService.getQnAById(questionId);
        log.info("[문의 단건 확인 API] : 완료");
        return ApiResponse.success(result);
    }

    // 문의 내역 삭제하기
    @DeleteMapping("/{questionId}")
    public ApiResponse<Void> deleteQuestion(@AuthenticationPrincipal UserDetails userDetails,
                                            @PathVariable Long questionId) {
        log.info("[문의 단건 삭제 API] : 요청");
        Long userId = Long.parseLong(userDetails.getUsername());
        qnAService.deleteQnAById(userId, questionId);
        log.info("[문의 단건 삭제 API] : 완료");
        return ApiResponse.success(null);
    }



}
