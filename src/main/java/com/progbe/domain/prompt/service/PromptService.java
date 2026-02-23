package com.progbe.domain.prompt.service;

import com.progbe.domain.category.entity.CategoryEntity;
import com.progbe.domain.category.repository.CategoryRepository;
import com.progbe.domain.prompt.dto.*;
import com.progbe.domain.prompt.entity.PromptEntity;
import com.progbe.domain.prompt.entity.PromptLikeEntity;
import com.progbe.domain.prompt.mapper.PromptMapper;
import com.progbe.domain.prompt.repository.PromptLikeRepository;
import com.progbe.domain.prompt.repository.PromptRepository;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.domain.user.repository.UserRepository;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PromptService {

    private final PromptRepository promptRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PromptLikeRepository promptLikeRepository;
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

    // 최신순 프롬프트 띄어주기 로직 (#31)
    public PromptListResponse getPromptsSortedTime(Pageable pageable) {
        Page<PromptEntity> promptEntities = promptRepository.findPromptSortedTime(pageable);

        List<PromptSummaryResponse> promptList = promptEntities.getContent().stream()
                .map(entity -> new PromptSummaryResponse(
                        entity.getId(),
                        promptMapper.toCategoryResponse(entity.getCategory()),
                        entity.getTitle(),
                        entity.getCreatedAt(),
                        entity.getUpdatedAt()
                ))
                .toList();

        return new PromptListResponse(
                promptList,
                promptEntities.getTotalElements()
        );
    }

    // 좋아요순 프롬프트 띄어주기 로직 (#31)
    public PromptListResponse getPromptsSortedLikeCount(Pageable pageable) {
        Page<PromptEntity> promptEntities = promptRepository.findPromptSortedLikeCount(pageable);

        List<PromptSummaryResponse> promptList = promptEntities.getContent().stream()
                .map(entity -> new PromptSummaryResponse(
                        entity.getId(),
                        promptMapper.toCategoryResponse(entity.getCategory()),
                        entity.getTitle(),
                        entity.getCreatedAt(),
                        entity.getUpdatedAt()
                ))
                .toList();

        return new PromptListResponse(
                promptList,
                promptEntities.getTotalElements()
        );
    }

    // 오늘의 좋아요를 가장 많이 받은 프롬프트 띄어주기 로직 (#31)
    // LocalDateTime 을 이용해서 오늘 (= 00시 ~ 23시 59분) 으로 설정했습니다.
    public List<PromptSummaryResponse> getDailyHotPrompts() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        Pageable topFive = PageRequest.of(0, 3);

        List<PromptEntity> promptEntities = promptRepository.findDailyHotPrompts(start, end, topFive);

        return promptMapper.toPromptSummaryResponseList(promptEntities);
    }

    // 좋아요 생성 (#32)
    // 좋아요 눌렀는지를 확인하기 위해 null 값이 허용되는 Optional 전략 사용
    public PromptLikeResponse likePrompt(Long promptId, Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        PromptEntity prompt = promptRepository.findById(promptId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROMPT_NOT_FOUND));

        Optional<PromptLikeEntity> existingLike = promptLikeRepository.findByUserAndPrompt(user, prompt);

        if (existingLike.isPresent()) {
            promptLikeRepository.delete(existingLike.get());
            return PromptLikeResponse.of(LikeStatus.UNLIKE);
        }

        PromptLikeEntity newLike = PromptLikeEntity.from(user, prompt);

        promptLikeRepository.save(newLike);
        return PromptLikeResponse.of(LikeStatus.LIKE);
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
