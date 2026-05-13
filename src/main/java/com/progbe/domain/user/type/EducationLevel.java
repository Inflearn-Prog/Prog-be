package com.progbe.domain.user.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EducationLevel {
    HIGH_SCHOOL("고졸"),
    ASSOCIATE("초대졸"),
    BACHELOR("4년제"),
    MASTER("석사"),
    DOCTOR("박사");

    private final String description;
}
