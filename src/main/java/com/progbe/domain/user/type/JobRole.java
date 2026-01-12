package com.progbe.domain.user.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JobRole {
    DEVELOPMENT("개발"),
    MARKETING_CONTENT("마케팅/콘텐츠"),
    SERVICE_PLANNING("서비스 기획"),
    HR_GA("인사/총무"),
    DESIGN("디자인"),
    ETC("기타");

    private final String description;
}
