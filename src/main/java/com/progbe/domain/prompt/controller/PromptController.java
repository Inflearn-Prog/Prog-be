package com.progbe.domain.prompt.controller;

import com.progbe.domain.prompt.dto.PromptCreateRequest;
import com.progbe.domain.prompt.dto.PromptListResponse;
import com.progbe.domain.prompt.dto.PromptResponse;
import com.progbe.domain.prompt.dto.PromptUpdateRequest;
import com.progbe.domain.prompt.service.PromptService;
import com.progbe.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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
        Long userId = Long.parseLong(userDetails.getUsername());
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
}

