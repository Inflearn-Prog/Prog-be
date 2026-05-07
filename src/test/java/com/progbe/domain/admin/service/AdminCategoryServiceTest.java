package com.progbe.domain.admin.service;

import com.progbe.domain.admin.category.dto.AdminCategoryRequest;
import com.progbe.domain.admin.category.dto.AdminCategoryResponse;
import com.progbe.domain.admin.category.service.AdminCategoryService;
import com.progbe.domain.category.entity.CategoryEntity;
import com.progbe.domain.category.repository.CategoryRepository;
import com.progbe.domain.prompt.repository.PromptRepository;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminCategoryServiceTest {

    @InjectMocks
    private AdminCategoryService adminCategoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PromptRepository promptRepository;

    private CategoryEntity parentCategory;
    private CategoryEntity childCategory;

    @BeforeEach
    void setUp() {
        parentCategory = CategoryEntity.builder()
                .name("개발")
                .description("개발 관련 카테고리")
                .displayOrder(0)
                .build();
        setEntityId(parentCategory, 1L);

        childCategory = CategoryEntity.builder()
                .name("백엔드")
                .description("백엔드 관련")
                .parent(parentCategory)
                .displayOrder(1)
                .build();
        setEntityId(childCategory, 2L);
    }

    @Nested
    @DisplayName("createCategory - 카테고리 생성")
    class CreateCategory {

        @Test
        @DisplayName("정상적으로 카테고리를 생성하고 displayOrder가 자동 할당된다")
        void createCategory_success() {
            // given
            AdminCategoryRequest.CreateRequest request =
                    new AdminCategoryRequest.CreateRequest("디자인", "디자인 카테고리", null);

            given(categoryRepository.findByNameAndNotDeleted("디자인")).willReturn(Optional.empty());
            given(categoryRepository.findMaxDisplayOrder()).willReturn(2);

            CategoryEntity savedCategory = CategoryEntity.builder()
                    .name("디자인")
                    .description("디자인 카테고리")
                    .displayOrder(3)
                    .build();
            setEntityId(savedCategory, 3L);

            given(categoryRepository.save(any(CategoryEntity.class))).willReturn(savedCategory);

            // when
            AdminCategoryResponse.CategoryCreateResponse response = adminCategoryService.createCategory(request);

            // then
            assertThat(response.categoryId()).isEqualTo(3L);
            assertThat(response.name()).isEqualTo("디자인");
            assertThat(response.parentId()).isNull();
            verify(categoryRepository).findMaxDisplayOrder();
        }

        @Test
        @DisplayName("이름이 중복되면 CATEGORY_NAME_DUPLICATE 예외가 발생한다")
        void createCategory_duplicateName() {
            // given
            AdminCategoryRequest.CreateRequest request =
                    new AdminCategoryRequest.CreateRequest("개발", "중복 이름", null);

            given(categoryRepository.findByNameAndNotDeleted("개발")).willReturn(Optional.of(parentCategory));

            // when & then
            assertThatThrownBy(() -> adminCategoryService.createCategory(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CATEGORY_NAME_DUPLICATE));
        }

        @Test
        @DisplayName("부모 카테고리를 지정하여 하위 카테고리를 생성한다")
        void createCategory_withParent() {
            // given
            AdminCategoryRequest.CreateRequest request =
                    new AdminCategoryRequest.CreateRequest("프론트엔드", "프론트엔드 관련", 1L);

            given(categoryRepository.findByNameAndNotDeleted("프론트엔드")).willReturn(Optional.empty());
            given(categoryRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(parentCategory));
            given(categoryRepository.findMaxDisplayOrder()).willReturn(1);

            CategoryEntity saved = CategoryEntity.builder()
                    .name("프론트엔드")
                    .description("프론트엔드 관련")
                    .parent(parentCategory)
                    .displayOrder(2)
                    .build();
            setEntityId(saved, 3L);

            given(categoryRepository.save(any(CategoryEntity.class))).willReturn(saved);

            // when
            AdminCategoryResponse.CategoryCreateResponse response = adminCategoryService.createCategory(request);

            // then
            assertThat(response.categoryId()).isEqualTo(3L);
            assertThat(response.parentId()).isEqualTo(1L);
            assertThat(response.parentName()).isEqualTo("개발");
        }

        @Test
        @DisplayName("부모가 이미 하위 카테고리이면 CATEGORY_DEPTH_EXCEEDED 예외가 발생한다")
        void createCategory_depthExceeded() {
            // given
            AdminCategoryRequest.CreateRequest request =
                    new AdminCategoryRequest.CreateRequest("스프링", "스프링 관련", 2L);

            given(categoryRepository.findByNameAndNotDeleted("스프링")).willReturn(Optional.empty());
            // childCategory는 parent가 있으므로 isParentCategory() == false
            given(categoryRepository.findByIdAndNotDeleted(2L)).willReturn(Optional.of(childCategory));

            // when & then
            assertThatThrownBy(() -> adminCategoryService.createCategory(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CATEGORY_DEPTH_EXCEEDED));
        }
    }

    @Nested
    @DisplayName("updateCategory - 카테고리 수정")
    class UpdateCategory {

        @Test
        @DisplayName("카테고리 이름과 설명을 정상적으로 수정한다")
        void updateCategory_success() {
            // given
            AdminCategoryRequest.UpdateRequest request =
                    new AdminCategoryRequest.UpdateRequest("개발2", "수정된 설명", null);

            given(categoryRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(parentCategory));
            given(categoryRepository.findByNameAndNotDeleted("개발2")).willReturn(Optional.empty());

            // when
            AdminCategoryResponse.CategoryUpdateResponse response =
                    adminCategoryService.updateCategory(1L, request);

            // then
            assertThat(response.categoryId()).isEqualTo(1L);
            assertThat(response.name()).isEqualTo("개발2");
        }
    }

    @Nested
    @DisplayName("deleteCategory - 카테고리 삭제")
    class DeleteCategory {

        @Test
        @DisplayName("카테고리를 정상적으로 삭제한다")
        void deleteCategory_success() {
            // given
            given(categoryRepository.findByIdAndNotDeleted(2L)).willReturn(Optional.of(childCategory));
            given(categoryRepository.existsChildrenByParentId(2L)).willReturn(false);
            given(promptRepository.existsByCategoryIdAndNotDeleted(2L)).willReturn(false);

            // when
            AdminCategoryResponse.CategoryDeleteResponse response =
                    adminCategoryService.deleteCategory(2L);

            // then
            assertThat(response.message()).contains("성공적으로 삭제");
        }

        @Test
        @DisplayName("하위 카테고리가 존재하면 CATEGORY_NOT_EMPTY 예외가 발생한다")
        void deleteCategory_hasChildren() {
            // given
            given(categoryRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(parentCategory));
            given(categoryRepository.existsChildrenByParentId(1L)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> adminCategoryService.deleteCategory(1L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CATEGORY_NOT_EMPTY));
        }

        @Test
        @DisplayName("프롬프트가 존재하면 CATEGORY_NOT_EMPTY 예외가 발생한다")
        void deleteCategory_hasPrompts() {
            // given
            given(categoryRepository.findByIdAndNotDeleted(2L)).willReturn(Optional.of(childCategory));
            given(categoryRepository.existsChildrenByParentId(2L)).willReturn(false);
            given(promptRepository.existsByCategoryIdAndNotDeleted(2L)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> adminCategoryService.deleteCategory(2L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CATEGORY_NOT_EMPTY));
        }
    }

    @Nested
    @DisplayName("updateCategoryOrder - 카테고리 순서 변경")
    class UpdateCategoryOrder {

        @Test
        @DisplayName("카테고리 순서를 정상적으로 변경한다")
        void updateCategoryOrder_success() {
            // given
            List<Long> categoryIds = List.of(2L, 1L);
            AdminCategoryRequest.UpdateOrderRequest request =
                    new AdminCategoryRequest.UpdateOrderRequest(categoryIds);

            given(categoryRepository.findAllByIdsAndNotDeleted(categoryIds))
                    .willReturn(List.of(parentCategory, childCategory));

            // when
            AdminCategoryResponse.CategoryOrderUpdateResponse response =
                    adminCategoryService.updateCategoryOrder(request);

            // then
            assertThat(response.updatedCount()).isEqualTo(2);
            assertThat(response.message()).contains("순서가 성공적으로 변경");
        }

        @Test
        @DisplayName("존재하지 않는 ID가 포함되면 CATEGORY_NOT_FOUND 예외가 발생한다")
        void updateCategoryOrder_notFoundId() {
            // given
            List<Long> categoryIds = List.of(1L, 999L);
            AdminCategoryRequest.UpdateOrderRequest request =
                    new AdminCategoryRequest.UpdateOrderRequest(categoryIds);

            given(categoryRepository.findAllByIdsAndNotDeleted(categoryIds))
                    .willReturn(List.of(parentCategory));  // 1개만 반환 → size 불일치

            // when & then
            assertThatThrownBy(() -> adminCategoryService.updateCategoryOrder(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.CATEGORY_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("getCategoryList - 카테고리 목록 조회")
    class GetCategoryList {

        @Test
        @DisplayName("전체 카테고리 목록을 조회한다")
        void getCategoryList_success() {
            // given
            // children 리스트가 비어있는 상태로 반환
            given(categoryRepository.findAllByNotDeleted())
                    .willReturn(List.of(parentCategory, childCategory));

            // when
            AdminCategoryResponse.CategoryListResponse response =
                    adminCategoryService.getCategoryList();

            // then
            assertThat(response.categories()).hasSize(2);
        }
    }

    /**
     * 리플렉션으로 엔티티 ID를 설정하는 헬퍼 메서드
     */
    private void setEntityId(CategoryEntity entity, Long id) {
        try {
            java.lang.reflect.Field idField = CategoryEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);

            // createdAt도 설정 (응답 변환에 필요)
            java.lang.reflect.Field createdAtField = findField(entity.getClass(), "createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(entity, java.time.LocalDateTime.now());

            java.lang.reflect.Field updatedAtField = findField(entity.getClass(), "updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(entity, java.time.LocalDateTime.now());
        } catch (Exception e) {
            throw new RuntimeException("엔티티 ID 설정 실패", e);
        }
    }

    private java.lang.reflect.Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException("Field not found: " + fieldName);
    }
}
