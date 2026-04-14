package com.progbe.domain.jobrole.controller;

import com.progbe.domain.jobrole.dto.JobRoleResponse;
import com.progbe.domain.jobrole.dto.JobRoleTreeResponse;
import com.progbe.domain.jobrole.dto.SaveUserJobRolesRequest;
import com.progbe.domain.jobrole.service.JobRoleService;
import com.progbe.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/job-roles")
@RequiredArgsConstructor
public class JobRoleController {

    private final JobRoleService jobRoleService;

    @GetMapping
    public ApiResponse<List<JobRoleTreeResponse>> getJobRoleTree() {
        return ApiResponse.success(jobRoleService.getFullTree());
    }

    @PostMapping("/me")
    public ApiResponse<Void> saveMyJobRoles(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SaveUserJobRolesRequest request
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        jobRoleService.saveUserJobRoles(userId, request.jobRoleIds());
        return ApiResponse.success(null);
    }

    @GetMapping("/me")
    public ApiResponse<List<JobRoleResponse>> getMyJobRoles(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ApiResponse.success(jobRoleService.getUserJobRoles(userId));
    }

}
