package com.progbe.domain.notice.service;

import com.progbe.domain.notice.dto.NoticeRequest;
import com.progbe.domain.notice.dto.NoticeResponse;
import com.progbe.domain.notice.entity.NoticeEntity;
import com.progbe.domain.notice.mapper.NoticeMapper;
import com.progbe.domain.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
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
}