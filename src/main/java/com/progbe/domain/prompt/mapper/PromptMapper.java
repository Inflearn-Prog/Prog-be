package com.progbe.domain.prompt.mapper;

import com.progbe.domain.category.dto.CategoryResponse;
import com.progbe.domain.category.entity.CategoryEntity;
import com.progbe.domain.prompt.dto.PromptResponse;
import com.progbe.domain.prompt.dto.PromptSummaryResponse;
import com.progbe.domain.prompt.entity.PromptEntity;
import com.progbe.domain.user.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PromptMapper {

    public PromptEntity toPromptEntity(UserEntity user, CategoryEntity category, String title, String content) {
        return PromptEntity.builder()
                .user(user)
                .category(category)
                .title(title)
                .content(content)
                .build();
    }

    public CategoryResponse toCategoryResponse(CategoryEntity category) {
        if (category == null) {
            return null;
        }
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }

    public PromptResponse toPromptResponse(PromptEntity prompt) {
        return new PromptResponse(
                prompt.getId(),
                prompt.getUser().getId(),
                toCategoryResponse(prompt.getCategory()),
                prompt.getTitle(),
                prompt.getContent(),
                prompt.getCreatedAt(),
                prompt.getUpdatedAt()
        );
    }

    public PromptSummaryResponse toPromptSummaryResponse(PromptEntity prompt) {
        return new PromptSummaryResponse(
                prompt.getId(),
                toCategoryResponse(prompt.getCategory()),
                prompt.getTitle(),
                prompt.getCreatedAt(),
                prompt.getUpdatedAt()
        );
    }

    public List<PromptSummaryResponse> toPromptSummaryResponseList(List<PromptEntity> prompts) {
        return prompts.stream()
                .map(this::toPromptSummaryResponse)
                .collect(Collectors.toList());
    }
}

