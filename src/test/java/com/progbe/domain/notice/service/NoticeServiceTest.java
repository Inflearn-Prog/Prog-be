package com.progbe.domain.notice.service;

import com.progbe.domain.notice.dto.NoticeRequest;
import com.progbe.domain.notice.dto.NoticeResponse;
import com.progbe.domain.notice.entity.NoticeEntity;
import com.progbe.domain.notice.mapper.NoticeMapper;
import com.progbe.domain.notice.repository.NoticeRepository;
import com.progbe.global.common.CommonResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @InjectMocks
    private NoticeService noticeService;

    @Mock
    private NoticeRepository noticeRepository;

    @Mock
    private NoticeMapper noticeMapper;

    private NoticeEntity noticeEntity;

    @BeforeEach
    void setUp() {
        noticeEntity = NoticeEntity.builder()
                .title("테스트 공지")
                .content("테스트 공지 내용입니다.")
                .build();
        setEntityId(noticeEntity, 1L);
    }

    @Nested
    @DisplayName("createNotice - 공지사항 생성")
    class CreateNotice {

        @Test
        @DisplayName("공지사항을 정상적으로 생성한다")
        void createNotice_success() {
            // given
            NoticeRequest.CreateNoticeRequest request =
                    new NoticeRequest.CreateNoticeRequest("새 공지", "새 공지 내용");

            given(noticeMapper.toEntity(request)).willReturn(noticeEntity);
            given(noticeRepository.save(noticeEntity)).willReturn(noticeEntity);
            given(noticeMapper.toCreateResponse(noticeEntity))
                    .willReturn(new NoticeResponse.CreateNoticeResponse(1L, "공지가 성공적으로 등록되었습니다."));

            // when
            NoticeResponse.CreateNoticeResponse response = noticeService.createNotice(request);

            // then
            assertThat(response.noticeId()).isEqualTo(1L);
            assertThat(response.message()).contains("성공적으로 등록");
            verify(noticeRepository).save(noticeEntity);
        }
    }

    @Nested
    @DisplayName("getNoticeList - 공지사항 목록 조회")
    class GetNoticeList {

        @Test
        @DisplayName("페이지네이션으로 공지사항 목록을 조회한다")
        void getNoticeList_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<NoticeEntity> noticePage = new PageImpl<>(
                    List.of(noticeEntity), pageable, 1
            );

            NoticeResponse.NoticeListResponse expectedResponse = new NoticeResponse.NoticeListResponse(
                    List.of(new NoticeResponse.NoticeInfo(1L, "테스트 공지", LocalDateTime.now())),
                    CommonResponse.PageInfoResponse.builder()
                            .currentPage(0)
                            .pageSize(10)
                            .totalPages(1)
                            .totalCount(1)
                            .build()
            );

            given(noticeRepository.findAllByNotDeleted(any(Pageable.class))).willReturn(noticePage);
            given(noticeMapper.toNoticeListResponse(noticePage)).willReturn(expectedResponse);

            // when
            NoticeResponse.NoticeListResponse response = noticeService.getNoticeList(0, 10);

            // then
            assertThat(response.content()).hasSize(1);
            assertThat(response.pageInfo().getTotalCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getNoticeDetail - 공지사항 상세 조회")
    class GetNoticeDetail {

        @Test
        @DisplayName("공지사항 상세를 정상적으로 조회한다")
        void getNoticeDetail_success() {
            // given
            given(noticeRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(noticeEntity));
            given(noticeMapper.toNoticeDetailResponse(noticeEntity))
                    .willReturn(new NoticeResponse.NoticeDetailResponse(1L, "테스트 공지", "테스트 공지 내용입니다.", LocalDateTime.now()));

            // when
            NoticeResponse.NoticeDetailResponse response = noticeService.getNoticeDetail(1L);

            // then
            assertThat(response.noticeId()).isEqualTo(1L);
            assertThat(response.title()).isEqualTo("테스트 공지");
        }

        @Test
        @DisplayName("존재하지 않는 공지사항 조회 시 NOTICE_NOT_FOUND 예외가 발생한다")
        void getNoticeDetail_notFound() {
            // given
            given(noticeRepository.findByIdAndNotDeleted(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> noticeService.getNoticeDetail(999L))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.NOTICE_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("updateNotice - 공지사항 수정")
    class UpdateNotice {

        @Test
        @DisplayName("공지사항을 정상적으로 수정한다")
        void updateNotice_success() {
            // given
            NoticeRequest.UpdateNoticeRequest request =
                    new NoticeRequest.UpdateNoticeRequest("수정된 제목", "수정된 내용");

            given(noticeRepository.findByIdAndNotDeleted(1L)).willReturn(Optional.of(noticeEntity));
            given(noticeMapper.toUpdateResponse(noticeEntity))
                    .willReturn(new NoticeResponse.UpdateNoticeResponse(1L, "공지사항이 성공적으로 수정되었습니다."));

            // when
            NoticeResponse.UpdateNoticeResponse response = noticeService.updateNotice(1L, request);

            // then
            assertThat(response.noticeId()).isEqualTo(1L);
            assertThat(response.message()).contains("성공적으로 수정");
        }

        @Test
        @DisplayName("존재하지 않는 공지사항 수정 시 NOTICE_NOT_FOUND 예외가 발생한다")
        void updateNotice_notFound() {
            // given
            NoticeRequest.UpdateNoticeRequest request =
                    new NoticeRequest.UpdateNoticeRequest("수정된 제목", "수정된 내용");

            given(noticeRepository.findByIdAndNotDeleted(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> noticeService.updateNotice(999L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.NOTICE_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("bulkDeleteNotices - 공지사항 일괄 삭제")
    class BulkDeleteNotices {

        @Test
        @DisplayName("공지사항을 일괄 삭제한다")
        void bulkDeleteNotices_success() {
            // given
            List<Long> noticeIds = List.of(1L, 2L, 3L);
            NoticeRequest.BulkDeleteRequest request = new NoticeRequest.BulkDeleteRequest(noticeIds);

            given(noticeRepository.bulkSoftDelete(eq(noticeIds), any(LocalDateTime.class))).willReturn(3);
            given(noticeMapper.toBulkDeleteResponse(3, 3))
                    .willReturn(new NoticeResponse.BulkDeleteResponse(3, "선택한 공지사항이 삭제되었습니다."));

            // when
            NoticeResponse.BulkDeleteResponse response = noticeService.bulkDeleteNotices(request);

            // then
            assertThat(response.deletedCount()).isEqualTo(3);
            assertThat(response.message()).contains("삭제");
        }
    }

    private void setEntityId(NoticeEntity entity, Long id) {
        try {
            java.lang.reflect.Field idField = NoticeEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);

            java.lang.reflect.Field createdAtField = findField(entity.getClass(), "createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(entity, LocalDateTime.now());
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
