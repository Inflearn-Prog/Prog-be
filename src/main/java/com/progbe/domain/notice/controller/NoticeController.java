package com.progbe.domain.notice.controller;

import com.progbe.domain.notice.dto.NoticeRequest;
import com.progbe.domain.notice.dto.NoticeResponse;
import com.progbe.domain.notice.service.NoticeService;
import com.progbe.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    public ResponseEntity<ApiResponse<NoticeResponse.NoticeListResponse>> getNoticeList(
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

    @PostMapping
    public ResponseEntity<ApiResponse<NoticeResponse.CreateNoticeResponse>> createNotice(
            @Valid @RequestBody NoticeRequest.CreateNoticeRequest request
    ) {
        NoticeResponse.CreateNoticeResponse response = noticeService.createNotice(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{noticeId}")
    public ResponseEntity<ApiResponse<NoticeResponse.UpdateNoticeResponse>> updateNotice(
            @PathVariable Long noticeId,
            @Valid @RequestBody NoticeRequest.UpdateNoticeRequest request
    ) {
        NoticeResponse.UpdateNoticeResponse response = noticeService.updateNotice(noticeId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/bulk")
    public ResponseEntity<ApiResponse<NoticeResponse.BulkDeleteResponse>> bulkDeleteNotices(
            @Valid @RequestBody NoticeRequest.BulkDeleteRequest request
    ) {
        NoticeResponse.BulkDeleteResponse response = noticeService.bulkDeleteNotices(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}