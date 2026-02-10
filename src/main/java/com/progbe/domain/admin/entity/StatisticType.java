package com.progbe.domain.admin.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum StatisticType {
    NEW_USERS("신규 유저"),
    NEW_PROMPTS("신규 프롬프트"),
    COPY_COUNT("복사 횟수");

    private final String description;
}