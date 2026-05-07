package com.progbe.domain.prompt.controller;

import com.progbe.domain.prompt.dto.*;
import com.progbe.domain.prompt.service.PromptService;
import com.progbe.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prompts")
@RequiredArgsConstructor
public class PromptController {

    private final PromptService promptService;

    /**
     * 프롬프트 게시글 생성
     *
     * @param userDetails
     * @param request
     * @return
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PromptResponse>> createPrompt(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PromptCreateRequest request
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        PromptResponse response = promptService.createPrompt(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프롬프트 게시글 조회
     *
     * @param userDetails
     * @param promptId
     * @return
     */
    @GetMapping("/{promptId}")
    public ResponseEntity<ApiResponse<PromptResponse>> getPrompt(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long promptId
    ) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        PromptResponse response = promptService.getPrompt(promptId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프롬프트 리스트 조회
     *
     * @param userDetails
     * @param pageable
     * @return
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PromptListResponse>> getPromptList(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        PromptListResponse response = promptService.getPromptList(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프롬프트 게시글 수정
     *
     * @param userDetails
     * @param promptId
     * @param request
     * @return
     */
    @PutMapping("/{promptId}")
    public ResponseEntity<ApiResponse<PromptResponse>> updatePrompt(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long promptId,
            @Valid @RequestBody PromptUpdateRequest request
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        PromptResponse response = promptService.updatePrompt(promptId, userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프롬프트 게시글 삭제
     *
     * @param userDetails
     * @param promptId
     * @return
     */
    @DeleteMapping("/{promptId}")
    public ResponseEntity<ApiResponse<Void>> deletePrompt(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long promptId
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        promptService.deletePrompt(promptId, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 프롬프트 최신순 API
     * @param pageable
     * @return
     */
    @GetMapping("/createDesc")
    public ApiResponse<PromptListResponse> createPromptDesc(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PromptListResponse prompts = promptService.getPromptsSortedTime(pageable);
        return ApiResponse.success(prompts);
    }

    /**
     * 프롬프트 좋아요순 API
     * @param pageable
     * @return
     */
    @GetMapping("/likeDesc")
    public ApiResponse<PromptListResponse> likePromptDesc(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PromptListResponse prompts = promptService.getPromptsSortedLikeCount(pageable);
        return ApiResponse.success(prompts);
    }

    /**
     * 금일 가장 많은 좋아요순 API
     * @return
     */
    @GetMapping("/today-hot")
    public ApiResponse<List<PromptSummaryResponse>> getTodayHotPrompts() {
        List<PromptSummaryResponse> prompts = promptService.getDailyHotPrompts();
        return ApiResponse.success(prompts);
    }


    /**
     * 좋아요 생성 및 취소 기능
     * @param promptId
     * @param userDetails
     * @return
     */
    @PostMapping("/{promptId}/like")
    public ApiResponse<PromptLikeResponse> likePrompt(@PathVariable Long promptId, @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        PromptLikeResponse likePrompt = promptService.likePrompt(promptId, userId);
        return ApiResponse.success(likePrompt);
    }

    /**
     * 프롬프트 제목 기준 검색 API
     * @param keyword
     * @param pageable
     * @return
     */
    @GetMapping("/search/{keyword}")
    public ApiResponse<PromptListResponse> searchPrompts(@PathVariable("keyword") String keyword, Pageable pageable) {
        PromptListResponse promptListResponse = promptService.searchPromptsByTitle(keyword, pageable);
        return ApiResponse.success(promptListResponse);
    }
}

