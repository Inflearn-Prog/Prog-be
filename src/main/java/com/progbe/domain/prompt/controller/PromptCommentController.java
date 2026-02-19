package com.progbe.domain.prompt.controller;

import com.progbe.domain.prompt.dto.PromptCommentRequest;
import com.progbe.domain.prompt.dto.PromptCommentResponse;
import com.progbe.domain.prompt.service.PromptCommentService;
import com.progbe.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comment")
@Slf4j
@RequiredArgsConstructor
public class PromptCommentController {

    private final PromptCommentService promptCommentService;

    // 댓글 생성 API
    @PostMapping("/{promptId}")
    public ApiResponse<PromptCommentResponse> createComment(@AuthenticationPrincipal UserDetails userDetails,
                                        @PathVariable("promptId") Long promptId,
                                        @RequestBody PromptCommentRequest promptCommentRequest){
        log.info("[댓글 생성 API] : 요청");
        Long userId = Long.parseLong(userDetails.getUsername());
        PromptCommentResponse result = promptCommentService.createComment(userId, promptId, promptCommentRequest);
        log.info("[댓글 생성 API] : 완료");
        return ApiResponse.success(result);
    }

    // 대댓글 생성 API (1depth)
    @PostMapping("/{promptId}/{commentId}")
    public ApiResponse<PromptCommentResponse> createReply(@AuthenticationPrincipal UserDetails userDetails,
                                                          @PathVariable("promptId") Long promptId,
                                      @PathVariable("commentId") Long commentId,
                                      @RequestBody PromptCommentRequest promptCommentRequest){
        log.info("[대댓글 생성 API] : 요청");
        Long userId = Long.parseLong(userDetails.getUsername());
        PromptCommentResponse result = promptCommentService.createReply(userId, promptId, commentId, promptCommentRequest);
        log.info("[대댓글 생성 API] : 완료");
        return ApiResponse.success(result);
    }

    // 댓글 수정 API
    @PatchMapping("/{commentId}")
    public ApiResponse<PromptCommentResponse> modifyComment(@AuthenticationPrincipal UserDetails userDetails,
                                        @PathVariable("commentId") Long commentId,
                                        @RequestBody PromptCommentRequest promptCommentRequest) {
        log.info("[댓글 수정 API] : 요청");
        Long userId = Long.parseLong(userDetails.getUsername());
        PromptCommentResponse result = promptCommentService.modifyComment(userId, commentId, promptCommentRequest);
        log.info("[댓글 수정 API] : 완료");
        return ApiResponse.success(result);
    }

    // 댓글 삭제 API
    @DeleteMapping("/{commentId}")
    public ApiResponse<?> deleteComment(@AuthenticationPrincipal UserDetails userDetails,
                                        @PathVariable("commentId") Long commentId) {
        log.info("[댓글 삭제 API] : 요청");
        Long userId = Long.parseLong(userDetails.getUsername());
        promptCommentService.deleteComment(userId, commentId);
        log.info("[댓글 삭제 API] : 완료");
        return ApiResponse.success(null);
    }

    // 댓글 읽기 (전체)
    @GetMapping("/{promptId}")
    public ApiResponse<Slice<PromptCommentResponse>> readComments(@PathVariable("promptId") Long promptId) {
        log.info("[댓글 읽기 API] : 요청");
        Slice<PromptCommentResponse> result = promptCommentService.readComments(promptId);
        log.info("[댓글 읽기 API] : 완료");
        return ApiResponse.success(result);
    }

}
