package com.progbe.domain.prompt.service;

import com.progbe.domain.category.entity.CategoryEntity;
import com.progbe.domain.category.repository.CategoryRepository;
import com.progbe.domain.prompt.dto.*;
import com.progbe.domain.prompt.entity.PromptEntity;
import com.progbe.domain.prompt.entity.PromptLikeEntity;
import com.progbe.domain.prompt.mapper.PromptMapper;
import com.progbe.domain.prompt.repository.PromptCommentRepository;
import com.progbe.domain.prompt.repository.PromptLikeRepository;
import com.progbe.domain.prompt.repository.PromptRepository;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.domain.user.repository.UserProfileRepository;
import com.progbe.domain.user.repository.UserRepository;
import com.progbe.domain.user.type.Role;
import com.progbe.domain.user.type.UserStatus;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PromptServiceLikeTest {

    @InjectMocks
    private PromptService promptService;

    @Mock private PromptRepository promptRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private PromptLikeRepository promptLikeRepository;
    @Mock private PromptCommentRepository promptCommentRepository;
    @Mock private UserProfileRepository userProfileRepository;
    @Spy  private PromptMapper promptMapper = new PromptMapper();

    // ===== Helpers =====

    private UserEntity createUser(Long id) {
        UserEntity user = UserEntity.builder()
                .nickname("user" + id)
                .email("user" + id + "@test.com")
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();
        try {
            var field = UserEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    private PromptEntity createPrompt(Long id, UserEntity author) {
        CategoryEntity category = CategoryEntity.builder()
                .name("카테고리")
                .description("설명")
                .displayOrder(1)
                .build();
        PromptEntity prompt = PromptEntity.builder()
                .user(author)
                .category(category)
                .title("프롬프트 " + id)
                .content("본문 내용 " + id)
                .build();
        try {
            var field = PromptEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(prompt, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return prompt;
    }

    private PromptLikeCountDto likeCountDto(Long promptId, Long count) {
        return new PromptLikeCountDto() {
            @Override public Long getPromptId() { return promptId; }
            @Override public Long getCount() { return count; }
        };
    }

    // ===== Cycle 1: 좋아요 생성 =====

    @Nested
    @DisplayName("likePrompt - 좋아요 토글")
    class LikePromptTest {

        @Test
        @DisplayName("좋아요가 없으면 생성하고 LIKE 상태를 반환한다")
        void likePrompt_whenNotLiked_savesAndReturnsLike() {
            Long userId = 1L, promptId = 10L;
            UserEntity user = createUser(userId);
            PromptEntity prompt = createPrompt(promptId, createUser(2L));

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(promptRepository.findByIdAndNotDeleted(promptId)).willReturn(Optional.of(prompt));
            given(promptLikeRepository.findByUserAndPrompt(user, prompt)).willReturn(Optional.empty());

            PromptLikeResponse response = promptService.likePrompt(promptId, userId);

            assertThat(response.likeStatus()).isEqualTo("LIKE");
            verify(promptLikeRepository).save(any(PromptLikeEntity.class));
        }

        // ===== Cycle 2: 좋아요 취소 =====

        @Test
        @DisplayName("좋아요가 있으면 취소하고 UNLIKE 상태를 반환한다")
        void likePrompt_whenAlreadyLiked_deletesAndReturnsUnlike() {
            Long userId = 1L, promptId = 10L;
            UserEntity user = createUser(userId);
            PromptEntity prompt = createPrompt(promptId, createUser(2L));
            PromptLikeEntity existingLike = PromptLikeEntity.from(user, prompt);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(promptRepository.findByIdAndNotDeleted(promptId)).willReturn(Optional.of(prompt));
            given(promptLikeRepository.findByUserAndPrompt(user, prompt)).willReturn(Optional.of(existingLike));

            PromptLikeResponse response = promptService.likePrompt(promptId, userId);

            assertThat(response.likeStatus()).isEqualTo("UNLIKE");
            verify(promptLikeRepository).delete(existingLike);
        }

        // ===== Cycle 3: 예외 =====

        @Test
        @DisplayName("존재하지 않는 유저이면 USER_NOT_FOUND 예외를 던진다")
        void likePrompt_whenUserNotFound_throwsException() {
            given(userRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> promptService.likePrompt(10L, 99L))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("존재하지 않는 프롬프트이면 PROMPT_NOT_FOUND 예외를 던진다")
        void likePrompt_whenPromptNotFound_throwsException() {
            Long userId = 1L;
            given(userRepository.findById(userId)).willReturn(Optional.of(createUser(userId)));
            given(promptRepository.findByIdAndNotDeleted(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> promptService.likePrompt(99L, userId))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PROMPT_NOT_FOUND);
        }
    }

    // ===== Cycle 4: 좋아요 목록 조회 =====

    @Nested
    @DisplayName("getLikedPromptsByUser - 좋아요한 프롬프트 목록 조회")
    class GetLikedPromptsByUserTest {

        @Test
        @DisplayName("본인이 조회하면 isLiked=true와 likeCount가 반영된 목록을 반환한다")
        void getLikedPromptsByUser_whenSelf_returnsListWithLikeCountAndIsLiked() {
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            UserEntity user = createUser(userId);
            PromptEntity prompt = createPrompt(10L, createUser(2L));
            Page<PromptEntity> page = new PageImpl<>(List.of(prompt), pageable, 1);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(promptLikeRepository.findLikedPromptsByUserId(userId, pageable)).willReturn(page);
            given(promptLikeRepository.findLikedPromptIdsByUserId(eq(userId), anyList()))
                    .willReturn(List.of(10L));
            given(promptLikeRepository.countByPromptIds(anyList()))
                    .willReturn(List.of(likeCountDto(10L, 5L)));

            PromptListResponse response = promptService.getLikedPromptsByUser(userId, userId, pageable);

            assertThat(response.totalCount()).isEqualTo(1);
            assertThat(response.prompts()).hasSize(1);
            assertThat(response.prompts().get(0).isLiked()).isTrue();
            assertThat(response.prompts().get(0).likeCount()).isEqualTo(5L);
        }

        @Test
        @DisplayName("타인이 조회하면 ACCESS_DENIED 예외를 던진다")
        void getLikedPromptsByUser_whenOtherUser_throwsAccessDenied() {
            Long targetId = 1L, requestId = 2L;
            given(userRepository.findById(targetId)).willReturn(Optional.of(createUser(targetId)));

            assertThatThrownBy(() -> promptService.getLikedPromptsByUser(targetId, requestId, PageRequest.of(0, 10)))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.ACCESS_DENIED);
        }
    }

    // ===== Cycle 5: 유저별 프롬프트 목록의 likeCount/isLiked =====

    @Nested
    @DisplayName("getPromptsByUser - likeCount/isLiked 반영")
    class GetPromptsByUserTest {

        @Test
        @DisplayName("좋아요한 프롬프트는 isLiked=true와 실제 likeCount를 반환한다")
        void getPromptsByUser_whenLiked_reflectsIsLikedAndLikeCount() {
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            UserEntity user = createUser(userId);
            PromptEntity prompt = createPrompt(10L, user);
            Page<PromptEntity> page = new PageImpl<>(List.of(prompt), pageable, 1);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(promptRepository.findAllByUserIdAndNotDeleted(userId, pageable)).willReturn(page);
            given(promptLikeRepository.findLikedPromptIdsByUserId(eq(userId), anyList()))
                    .willReturn(List.of(10L));
            given(promptLikeRepository.countByPromptIds(anyList()))
                    .willReturn(List.of(likeCountDto(10L, 3L)));

            PromptListResponse response = promptService.getPromptsByUser(userId, userId, pageable);

            assertThat(response.prompts().get(0).isLiked()).isTrue();
            assertThat(response.prompts().get(0).likeCount()).isEqualTo(3L);
        }

        @Test
        @DisplayName("좋아요하지 않은 프롬프트는 isLiked=false, likeCount=0을 반환한다")
        void getPromptsByUser_whenNotLiked_returnsIsLikedFalseAndZeroCount() {
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            UserEntity user = createUser(userId);
            PromptEntity prompt = createPrompt(10L, user);
            Page<PromptEntity> page = new PageImpl<>(List.of(prompt), pageable, 1);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(promptRepository.findAllByUserIdAndNotDeleted(userId, pageable)).willReturn(page);
            given(promptLikeRepository.findLikedPromptIdsByUserId(eq(userId), anyList()))
                    .willReturn(List.of());
            given(promptLikeRepository.countByPromptIds(anyList()))
                    .willReturn(List.of());

            PromptListResponse response = promptService.getPromptsByUser(userId, userId, pageable);

            assertThat(response.prompts().get(0).isLiked()).isFalse();
            assertThat(response.prompts().get(0).likeCount()).isEqualTo(0L);
        }
    }
}
