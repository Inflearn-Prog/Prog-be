package com.progbe.domain.notice.controller;

import com.progbe.domain.notice.dto.NoticeResponse;
import com.progbe.domain.notice.service.NoticeService;
import com.progbe.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
public class UserNoticeController {

    private final NoticeService noticeService;

    @GetMapping
    public ResponseEntity<ApiResponse<NoticeResponse.NoticeListResponse>> getNotices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        NoticeResponse.NoticeListResponse response = noticeService.getNoticeList(page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<NoticeResponse.NoticeDetailResponse>> getNoticeDetail(
            @PathVariable Long noticeId
    ) {
        NoticeResponse.NoticeDetailResponse response = noticeService.getNoticeDetail(noticeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
