package com.progbe.domain.admin.entity;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum StatisticType {
    Login,
    Visitor,
    Prompt,
    Copy
}
