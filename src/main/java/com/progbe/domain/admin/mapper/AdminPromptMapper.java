package com.progbe.domain.admin.mapper;

import com.progbe.domain.admin.dto.AdminPromptResponse;
import com.progbe.domain.admin.dto.PromptAdminDto;
import com.progbe.global.common.CommonMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AdminPromptMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yy.MM.dd");

    public AdminPromptResponse.PromptListResponse toPromptListResponse(Page<PromptAdminDto> promptPage) {
        List<AdminPromptResponse.PromptInfo> promptInfoList = promptPage.getContent().stream()
                .map(dto -> AdminPromptResponse.PromptInfo.builder()
                        .promptId(dto.promptId())
                        .title(dto.title())
                        .authorNickname(dto.authorNickname())
                        .categoryName(dto.categoryName())
                        .status(dto.status())
                        .createdAt(dto.createdAt().format(DATE_FORMATTER))
                        .build()
                )
                .collect(Collectors.toList());

        return AdminPromptResponse.PromptListResponse.builder()
                .content(promptInfoList)
                .pageInfo(CommonMapper.toPageInfoResponse(promptPage))
                .build();
    }

    public AdminPromptResponse.BulkUpdateResponse toBulkUpdateResponse(int updatedCount) {
        return AdminPromptResponse.BulkUpdateResponse.builder()
                .updatedCount(updatedCount)
                .message("선택한 게시글의 정보가 수정되었습니다.")
                .build();
    }

    public AdminPromptResponse.BulkDeleteResponse toBulkDeleteResponse(int deletedCount) {
        return AdminPromptResponse.BulkDeleteResponse.builder()
                .deletedCount(deletedCount)
                .message("선택한 게시글이 성공적으로 삭제(비활성화)되었습니다.")
                .build();
    }
}
