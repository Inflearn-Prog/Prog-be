package com.progbe.domain.prompt.service;

import com.progbe.domain.category.entity.CategoryEntity;
import com.progbe.domain.category.repository.CategoryRepository;
import com.progbe.domain.prompt.dto.*;
import com.progbe.domain.prompt.entity.PromptEntity;
import com.progbe.domain.prompt.mapper.PromptMapper;
import com.progbe.domain.prompt.repository.PromptRepository;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.domain.user.repository.UserRepository;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PromptService {

    private final PromptRepository promptRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PromptMapper promptMapper;

    /**
     * 프롬프트 게시글 등록 API입니다.
     *
     * @param userId
     * @param request
     * @return
     */
    @Transactional
    public PromptResponse createPrompt(Long userId, PromptCreateRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        CategoryEntity category = categoryRepository.findByIdAndNotDeleted(request.categoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        PromptEntity prompt = promptMapper.toPromptEntity(
                user,
                category,
                request.title(),
                request.content()
        );

        PromptEntity savedPrompt = promptRepository.save(prompt);
        return promptMapper.toPromptResponse(savedPrompt);
    }

    @Transactional(readOnly = true)
    public PromptResponse getPrompt(Long promptId, Long userId) {
        PromptEntity prompt = promptRepository.findByIdAndNotDeleted(promptId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROMPT_NOT_FOUND));

        if (!prompt.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        return promptMapper.toPromptResponse(prompt);
    }

    @Transactional(readOnly = true)
    public PromptListResponse getPromptList(Long userId, Pageable pageable) {
        Page<PromptEntity> promptPage = promptRepository.findAllByUserIdAndNotDeleted(userId, pageable);
        List<PromptSummaryResponse> promptSummaries = promptMapper.toPromptSummaryResponseList(promptPage.getContent());
        long totalCount = promptPage.getTotalElements();

        return new PromptListResponse(promptSummaries, totalCount);
    }

    @Transactional
    public PromptResponse updatePrompt(Long promptId, Long userId, PromptUpdateRequest request) {
        PromptEntity prompt = promptRepository.findByIdAndNotDeleted(promptId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROMPT_NOT_FOUND));

        if (!prompt.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        CategoryEntity category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findByIdAndNotDeleted(request.categoryId())
                    .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
        }

        prompt.update(category, request.title(), request.content());
        PromptEntity updatedPrompt = promptRepository.save(prompt);

        return promptMapper.toPromptResponse(updatedPrompt);
    }

    @Transactional
    public void deletePrompt(Long promptId, Long userId) {
        PromptEntity prompt = promptRepository.findByIdAndNotDeleted(promptId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROMPT_NOT_FOUND));

        if (!prompt.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        prompt.delete();
        promptRepository.save(prompt);
    }

    // 프롬프트 검색 로직 (#33)
    public PromptListResponse searchPromptsByTitle(String keyword, Pageable pageable) {
        Page<PromptEntity> searchResult = promptRepository.findByTitleContaining(keyword, pageable);

        return new PromptListResponse(
                searchResult.getContent().stream()
                        .map(PromptSummaryResponse::of)
                        .toList(),
                searchResult.getTotalElements()
        );
    }
}
