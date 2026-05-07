package com.progbe.domain.admin.service;

import com.progbe.domain.admin.dto.AdminPromptRequest;
import com.progbe.domain.admin.dto.AdminPromptResponse;
import com.progbe.domain.admin.dto.PromptAdminDto;
import com.progbe.domain.admin.mapper.AdminPromptMapper;
import com.progbe.domain.category.repository.CategoryRepository;
import com.progbe.domain.prompt.repository.PromptRepository;
import com.progbe.domain.prompt.type.PromptStatus;
import com.progbe.global.common.CommonResponse;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminPromptServiceTest {

    @InjectMocks
    private AdminPromptService adminPromptService;

    @Mock
    private PromptRepository promptRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AdminPromptMapper adminPromptMapper;

    @Nested
    @DisplayName("getPromptList - 프롬프트 목록 필터 조회")
    class GetPromptList {

        @Test
        @DisplayName("키워드, 카테고리, 상태 필터로 프롬프트 목록을 조회한다")
        void getPromptList_withFilters() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            PromptAdminDto dto = new PromptAdminDto(
                    1L, "테스트 프롬프트", "작성자", "개발", PromptStatus.PUBLIC, LocalDateTime.now()
            );
            Page<PromptAdminDto> promptPage = new PageImpl<>(List.of(dto), pageable, 1);

            AdminPromptResponse.PromptListResponse expectedResponse = AdminPromptResponse.PromptListResponse.builder()
                    .content(List.of(AdminPromptResponse.PromptInfo.builder()
                            .promptId(1L)
                            .title("테스트 프롬프트")
                            .authorNickname("작성자")
                            .categoryName("개발")
                            .status(PromptStatus.PUBLIC)
                            .createdAt("26.04.14")
                            .build()))
                    .pageInfo(CommonResponse.PageInfoResponse.builder()
                            .currentPage(0)
                            .pageSize(10)
                            .totalPages(1)
                            .totalCount(1)
                            .build())
                    .build();

            given(promptRepository.findByFiltersAsDto(
                    eq("테스트"), eq(1L), eq(PromptStatus.PUBLIC), any(Pageable.class)
            )).willReturn(promptPage);
            given(adminPromptMapper.toPromptListResponse(promptPage)).willReturn(expectedResponse);

            // when
            AdminPromptResponse.PromptListResponse response =
                    adminPromptService.getPromptList("테스트", 1L, PromptStatus.PUBLIC, 0, 10);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getPromptId()).isEqualTo(1L);
            assertThat(response.getPageInfo().getTotalCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("bulkUpdatePrompts - 프롬프트 일괄 상태 변경")
    class BulkUpdatePrompts {

        @Test
        @DisplayName("프롬프트 상태를 일괄 변경한다")
        void bulkUpdatePrompts_statusChange() {
            // given
            List<Long> promptIds = List.of(1L, 2L);
            AdminPromptRequest.UpdateFields updateFields =
                    new AdminPromptRequest.UpdateFields(null, PromptStatus.PRIVATE);
            AdminPromptRequest.BulkUpdateRequest request =
                    new AdminPromptRequest.BulkUpdateRequest(promptIds, updateFields);

            given(promptRepository.findExistingPromptIds(promptIds)).willReturn(promptIds);
            given(promptRepository.bulkUpdateStatus(promptIds, PromptStatus.PRIVATE)).willReturn(2);
            given(adminPromptMapper.toBulkUpdateResponse(2))
                    .willReturn(AdminPromptResponse.BulkUpdateResponse.builder()
                            .updatedCount(2)
                            .message("선택한 게시글의 정보가 수정되었습니다.")
                            .build());

            // when
            AdminPromptResponse.BulkUpdateResponse response =
                    adminPromptService.bulkUpdatePrompts(request);

            // then
            assertThat(response.getUpdatedCount()).isEqualTo(2);
            verify(promptRepository).bulkUpdateStatus(promptIds, PromptStatus.PRIVATE);
        }

        @Test
        @DisplayName("카테고리와 상태를 동시에 변경한다")
        void bulkUpdatePrompts_categoryAndStatus() {
            // given
            List<Long> promptIds = List.of(1L);
            AdminPromptRequest.UpdateFields updateFields =
                    new AdminPromptRequest.UpdateFields(5L, PromptStatus.PRIVATE);
            AdminPromptRequest.BulkUpdateRequest request =
                    new AdminPromptRequest.BulkUpdateRequest(promptIds, updateFields);

            given(promptRepository.findExistingPromptIds(promptIds)).willReturn(promptIds);
            given(categoryRepository.findByIdAndNotDeleted(5L)).willReturn(Optional.of(
                    com.progbe.domain.category.entity.CategoryEntity.builder().name("테스트").build()
            ));
            given(promptRepository.bulkUpdateCategory(promptIds, 5L)).willReturn(1);
            given(promptRepository.bulkUpdateStatus(promptIds, PromptStatus.PRIVATE)).willReturn(1);
            given(adminPromptMapper.toBulkUpdateResponse(1))
                    .willReturn(AdminPromptResponse.BulkUpdateResponse.builder()
                            .updatedCount(1)
                            .message("선택한 게시글의 정보가 수정되었습니다.")
                            .build());

            // when
            AdminPromptResponse.BulkUpdateResponse response =
                    adminPromptService.bulkUpdatePrompts(request);

            // then
            assertThat(response.getUpdatedCount()).isEqualTo(1);
            verify(promptRepository).bulkUpdateCategory(promptIds, 5L);
            verify(promptRepository).bulkUpdateStatus(promptIds, PromptStatus.PRIVATE);
        }

        @Test
        @DisplayName("존재하지 않는 프롬프트 ID만 있으면 PROMPTS_NOT_FOUND 예외가 발생한다")
        void bulkUpdatePrompts_allNotFound() {
            // given
            List<Long> promptIds = List.of(999L);
            AdminPromptRequest.UpdateFields updateFields =
                    new AdminPromptRequest.UpdateFields(null, PromptStatus.PRIVATE);
            AdminPromptRequest.BulkUpdateRequest request =
                    new AdminPromptRequest.BulkUpdateRequest(promptIds, updateFields);

            given(promptRepository.findExistingPromptIds(promptIds)).willReturn(List.of());

            // when & then
            assertThatThrownBy(() -> adminPromptService.bulkUpdatePrompts(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PROMPTS_NOT_FOUND));
        }

        @Test
        @DisplayName("유효하지 않은 카테고리로 변경 시 INVALID_CATEGORY 예외가 발생한다")
        void bulkUpdatePrompts_invalidCategory() {
            // given
            List<Long> promptIds = List.of(1L);
            AdminPromptRequest.UpdateFields updateFields =
                    new AdminPromptRequest.UpdateFields(999L, null);
            AdminPromptRequest.BulkUpdateRequest request =
                    new AdminPromptRequest.BulkUpdateRequest(promptIds, updateFields);

            given(promptRepository.findExistingPromptIds(promptIds)).willReturn(promptIds);
            given(categoryRepository.findByIdAndNotDeleted(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminPromptService.bulkUpdatePrompts(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_CATEGORY));
        }
    }

    @Nested
    @DisplayName("bulkDeletePrompts - 프롬프트 일괄 삭제")
    class BulkDeletePrompts {

        @Test
        @DisplayName("프롬프트를 일괄 soft delete 처리한다")
        void bulkDeletePrompts_success() {
            // given
            List<Long> promptIds = List.of(1L, 2L, 3L);
            AdminPromptRequest.BulkDeleteRequest request =
                    new AdminPromptRequest.BulkDeleteRequest(promptIds);

            given(promptRepository.findExistingPromptIds(promptIds)).willReturn(promptIds);
            given(promptRepository.bulkSoftDelete(eq(promptIds), any(LocalDateTime.class), eq(PromptStatus.DELETED)))
                    .willReturn(3);
            given(adminPromptMapper.toBulkDeleteResponse(3))
                    .willReturn(AdminPromptResponse.BulkDeleteResponse.builder()
                            .deletedCount(3)
                            .message("선택한 게시글이 성공적으로 삭제(비활성화)되었습니다.")
                            .build());

            // when
            AdminPromptResponse.BulkDeleteResponse response =
                    adminPromptService.bulkDeletePrompts(request);

            // then
            assertThat(response.getDeletedCount()).isEqualTo(3);
            assertThat(response.getMessage()).contains("삭제");
            verify(promptRepository).bulkSoftDelete(eq(promptIds), any(LocalDateTime.class), eq(PromptStatus.DELETED));
        }

        @Test
        @DisplayName("존재하지 않는 프롬프트만 있으면 PROMPTS_NOT_FOUND 예외가 발생한다")
        void bulkDeletePrompts_allNotFound() {
            // given
            List<Long> promptIds = List.of(999L);
            AdminPromptRequest.BulkDeleteRequest request =
                    new AdminPromptRequest.BulkDeleteRequest(promptIds);

            given(promptRepository.findExistingPromptIds(promptIds)).willReturn(List.of());

            // when & then
            assertThatThrownBy(() -> adminPromptService.bulkDeletePrompts(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.PROMPTS_NOT_FOUND));
        }
    }
}
