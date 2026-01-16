package com.progbe.domain.user.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CareerStatus {
    STUDENT("재학중"),
    JOB_SEEKER("취업 준비"),
    CAREER_CHANGE_PREP("이직 준비"),
    ETC("기타");

    private final String description;
}
