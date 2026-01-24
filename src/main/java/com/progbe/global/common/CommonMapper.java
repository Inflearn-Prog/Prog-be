package com.progbe.global.common;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


public class CommonMapper {
    public static String toRelativeTime(LocalDateTime time) {
        Duration duration = Duration.between(time, LocalDateTime.now());

        if (duration.toHours() < 24) return duration.toHours() + "hrs ago";
        return duration.toDays() + "day ago";
    }
    public static CommonResponse.PageInfoResponse toPageInfoResponse(Page<?> page) {
        return CommonResponse.PageInfoResponse.builder()
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalCount((int) page.getTotalElements())
                .build();
    }

    public static String toDate(LocalDateTime time) {
        if (time == null) return "-";

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH);

        return time.format(formatter);
    }
}
