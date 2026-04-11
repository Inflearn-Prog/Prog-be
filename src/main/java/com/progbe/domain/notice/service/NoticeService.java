package com.progbe.domain.notice.service;

import com.progbe.domain.notice.dto.NoticeRequest;
import com.progbe.domain.notice.dto.NoticeResponse;
import com.progbe.domain.notice.entity.NoticeEntity;
import com.progbe.domain.notice.mapper.NoticeMapper;
import com.progbe.domain.notice.repository.NoticeRepository;
import com.progbe.global.error.ErrorCode;
import com.progbe.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeMapper noticeMapper;

    @Transactional
    public NoticeResponse.CreateNoticeResponse createNotice(NoticeRequest.CreateNoticeRequest request) {
        NoticeEntity notice = noticeMapper.toEntity(request);
        NoticeEntity savedNotice = noticeRepository.save(notice);

        return noticeMapper.toCreateResponse(savedNotice);
    }

    @Transactional
    public NoticeResponse.BulkDeleteResponse bulkDeleteNotices(NoticeRequest.BulkDeleteRequest request) {
        List<Long> noticeIds = request.noticeIds();
        LocalDateTime now = LocalDateTime.now();

        int deletedCount = noticeRepository.bulkSoftDelete(noticeIds, now);
        return noticeMapper.toBulkDeleteResponse(noticeIds.size(), deletedCount);
    }

    @Transactional(readOnly = true)
    public NoticeResponse.NoticeListResponse getNoticeList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NoticeEntity> noticePage = noticeRepository.findAllByNotDeleted(pageable);
        return noticeMapper.toNoticeListResponse(noticePage);
    }

    @Transactional(readOnly = true)
    public NoticeResponse.NoticeDetailResponse getNoticeDetail(Long noticeId) {
        NoticeEntity notice = noticeRepository.findByIdAndNotDeleted(noticeId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTICE_NOT_FOUND));
        return noticeMapper.toNoticeDetailResponse(notice);
    }

    @Transactional
    public NoticeResponse.UpdateNoticeResponse updateNotice(Long noticeId, NoticeRequest.UpdateNoticeRequest request) {
        NoticeEntity notice = noticeRepository.findByIdAndNotDeleted(noticeId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTICE_NOT_FOUND));
        notice.update(request.title(), request.content());
        return noticeMapper.toUpdateResponse(notice);
    }
}