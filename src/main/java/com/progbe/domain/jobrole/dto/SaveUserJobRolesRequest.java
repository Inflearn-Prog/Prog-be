package com.progbe.domain.jobrole.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SaveUserJobRolesRequest(
        @NotEmpty(message = "직무를 1개 이상 선택해주세요.")
        @Size(max = 10, message = "직무는 최대 10개까지 선택 가능합니다.") //TODO : 추후 기획 수정 가능
        List<Long> jobRoleIds
) {}
