package com.progbe.domain.report.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportReason {
    SPAM_AD("광고나 도배예요"),
    INAPPROPRIATE_EXPRESSION("불쾌한 표현이 있어요"),
    NOT_WORKING("프롬프트가 작동하지 않아요"),
    PLAGIARISM("다른 사람의 프롬프트를 도용했어요"),
    OTHER("기타");

    private final String displayText;
}
