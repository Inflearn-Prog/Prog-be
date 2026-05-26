package com.progbe.domain.prompt.service;

import com.progbe.domain.category.entity.CategoryEntity;
import com.progbe.domain.category.repository.CategoryRepository;
import com.progbe.domain.prompt.dto.*;
import com.progbe.domain.prompt.entity.PromptEntity;
import com.progbe.domain.prompt.entity.PromptLikeEntity;
import com.progbe.domain.prompt.mapper.PromptMapper;
import com.progbe.domain.prompt.repository.PromptLikeRepository;
import com.progbe.domain.prompt.repository.PromptRepository;
import com.progbe.domain.prompt.type.PromptStatus;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.domain.user.entity.UserProfileEntity;
import com.progbe.domain.user.repository.UserProfileRepository;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromptService {

    private final PromptRepository promptRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PromptLikeRepository promptLikeRepository;
    private final UserProfileRepository userProfileRepository;
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

        String userDesc = userProfileRepository.findById(userId)
                .map(UserProfileEntity::getBio).orElse(null);

        return promptMapper.toPromptResponse(savedPrompt, userDesc, false, 0);
    }

    @Transactional(readOnly = true)
    public PromptResponse getPrompt(Long promptId, Long userId) {
        PromptEntity prompt = promptRepository.findByIdAndNotDeleted(promptId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROMPT_NOT_FOUND));

        if (prompt.getStatus() == PromptStatus.PRIVATE) {
            if (userId == null || !prompt.getUser().getId().equals(userId)) {
                throw new CustomException(ErrorCode.ACCESS_DENIED);
            }
        }

        return buildPromptResponse(prompt, userId);
    }

    @Transactional(readOnly = true)
    public PromptListResponse getPromptList(Long userId, Pageable pageable) {
        Page<PromptEntity> promptPage = promptRepository.findAllByUserIdAndNotDeleted(userId, pageable);
        List<PromptEntity> prompts = promptPage.getContent();
        Set<Long> likedIds = getLikedPromptIds(userId, prompts);
        Map<Long, Long> likeCountMap = getLikeCountMap(prompts);
        List<PromptSummaryResponse> promptSummaries = promptMapper.toPromptSummaryResponseList(prompts, likedIds, likeCountMap);

        return new PromptListResponse(promptSummaries, promptPage.getTotalElements());
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

        return buildPromptResponse(updatedPrompt, userId);
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
    @Transactional(readOnly = true)
    public PromptListResponse getPromptsSortedTime(Long categoryId, Long userId, Pageable pageable) {
        Page<PromptEntity> promptEntities = promptRepository.findPromptSortedTime(categoryId, pageable);
        List<PromptEntity> prompts = promptEntities.getContent();
        Set<Long> likedIds = getLikedPromptIds(userId, prompts);
        Map<Long, Long> likeCountMap = getLikeCountMap(prompts);

        return new PromptListResponse(
                promptMapper.toPromptSummaryResponseList(prompts, likedIds, likeCountMap),
                promptEntities.getTotalElements()
        );
    }

    // 좋아요순 프롬프트 띄어주기 로직 (#31)
    @Transactional(readOnly = true)
    public PromptListResponse getPromptsSortedLikeCount(Long categoryId, Long userId, Pageable pageable) {
        Page<PromptEntity> promptEntities = promptRepository.findPromptSortedLikeCount(categoryId, pageable);
        List<PromptEntity> prompts = promptEntities.getContent();
        Set<Long> likedIds = getLikedPromptIds(userId, prompts);
        Map<Long, Long> likeCountMap = getLikeCountMap(prompts);

        return new PromptListResponse(
                promptMapper.toPromptSummaryResponseList(prompts, likedIds, likeCountMap),
                promptEntities.getTotalElements()
        );
    }

    // 오늘의 좋아요를 가장 많이 받은 프롬프트 띄어주기 로직 (#31)
    // LocalDateTime 을 이용해서 오늘 (= 00시 ~ 23시 59분) 으로 설정했습니다.
    @Transactional(readOnly = true)
    public List<PromptSummaryResponse> getDailyHotPrompts(Long userId) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        Pageable topFive = PageRequest.of(0, 5);

        List<PromptEntity> promptEntities = promptRepository.findDailyHotPrompts(start, end, topFive);
        Set<Long> likedIds = getLikedPromptIds(userId, promptEntities);
        Map<Long, Long> likeCountMap = getLikeCountMap(promptEntities);

        return promptMapper.toPromptSummaryResponseList(promptEntities, likedIds, likeCountMap);
    }

    // 좋아요 생성 (#32)
    // 좋아요 눌렀는지를 확인하기 위해 null 값이 허용되는 Optional 전략 사용
    @Transactional
    public PromptLikeResponse likePrompt(Long promptId, Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        PromptEntity prompt = promptRepository.findByIdAndNotDeleted(promptId)
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
    @Transactional(readOnly = true)
    public PromptListResponse searchPromptsByTitle(String keyword, Long userId, Pageable pageable) {
        Page<PromptEntity> searchResult = promptRepository.findByTitleContaining(keyword, pageable);
        List<PromptEntity> prompts = searchResult.getContent();
        Set<Long> likedIds = getLikedPromptIds(userId, prompts);
        Map<Long, Long> likeCountMap = getLikeCountMap(prompts);

        return new PromptListResponse(
                promptMapper.toPromptSummaryResponseList(prompts, likedIds, likeCountMap),
                searchResult.getTotalElements()
        );
    }

    @Transactional(readOnly = true)
    public PromptListResponse getLikedPromptsByUser(Long targetUserId, Long requestUserId, Pageable pageable) {
        userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!targetUserId.equals(requestUserId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        Page<PromptEntity> promptPage = promptLikeRepository.findLikedPromptsByUserId(targetUserId, pageable);
        List<PromptEntity> prompts = promptPage.getContent();
        Set<Long> likedIds = getLikedPromptIds(requestUserId, prompts);
        Map<Long, Long> likeCountMap = getLikeCountMap(prompts);
        List<PromptSummaryResponse> summaries = promptMapper.toPromptSummaryResponseList(prompts, likedIds, likeCountMap);

        return new PromptListResponse(summaries, promptPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PromptListResponse getPromptsByUser(Long targetUserId, Long requestUserId, Pageable pageable) {
        userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Page<PromptEntity> promptPage;
        if (targetUserId.equals(requestUserId)) {
            promptPage = promptRepository.findAllByUserIdAndNotDeleted(targetUserId, pageable);
        } else {
            promptPage = promptRepository.findPublicByUserIdAndNotDeleted(targetUserId, pageable);
        }

        Set<Long> likedIds = getLikedPromptIds(requestUserId, promptPage.getContent());
        Map<Long, Long> likeCountMap = getLikeCountMap(promptPage.getContent());
        List<PromptSummaryResponse> summaries = promptMapper.toPromptSummaryResponseList(promptPage.getContent(), likedIds, likeCountMap);

        return new PromptListResponse(summaries, promptPage.getTotalElements());
    }

    private Set<Long> getLikedPromptIds(Long userId, List<PromptEntity> prompts) {
        if (userId == null || prompts.isEmpty()) {
            return Collections.emptySet();
        }
        List<Long> promptIds = prompts.stream().map(PromptEntity::getId).toList();
        return new HashSet<>(promptLikeRepository.findLikedPromptIdsByUserId(userId, promptIds));
    }

    private Map<Long, Long> getLikeCountMap(List<PromptEntity> prompts) {
        if (prompts.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> promptIds = prompts.stream().map(PromptEntity::getId).toList();
        return promptLikeRepository.countByPromptIds(promptIds).stream()
                .collect(Collectors.toMap(dto -> dto.getPromptId(), dto -> dto.getCount()));
    }

    private PromptResponse buildPromptResponse(PromptEntity prompt, Long requestUserId) {
        Long authorId = prompt.getUser().getId();
        String userDesc = userProfileRepository.findById(authorId)
                .map(UserProfileEntity::getBio).orElse(null);
        boolean isLiked = requestUserId != null
                && promptLikeRepository.existsByUserIdAndPromptId(requestUserId, prompt.getId());
        long likes = promptLikeRepository.countByPromptId(prompt.getId());
        return promptMapper.toPromptResponse(prompt, userDesc, isLiked, likes);
    }
}
