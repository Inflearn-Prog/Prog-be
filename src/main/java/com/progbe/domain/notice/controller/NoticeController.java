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

    @PostMapping
    public ResponseEntity<ApiResponse<NoticeResponse.CreateNoticeResponse>> createNotice(
            @Valid @RequestBody NoticeRequest.CreateNoticeRequest request
    ) {
        NoticeResponse.CreateNoticeResponse response = noticeService.createNotice(request);
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