package com.progbe.domain.prompt.mapper;

import com.progbe.domain.category.dto.CategoryResponse;
import com.progbe.domain.category.entity.CategoryEntity;
import com.progbe.domain.prompt.dto.PromptResponse;
import com.progbe.domain.prompt.dto.PromptSummaryResponse;
import com.progbe.domain.prompt.entity.PromptEntity;
import com.progbe.domain.user.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
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

    public PromptResponse toPromptResponse(PromptEntity prompt, String userDesc, boolean isLiked, long likes) {
        UserEntity user = prompt.getUser();
        return new PromptResponse(
                prompt.getId(),
                user.getId(),
                toCategoryResponse(prompt.getCategory()),
                prompt.getTitle(),
                prompt.getContent(),
                user.getNickname(),
                user.getProfileUrl(),
                userDesc,
                isLiked,
                (int) likes,
                prompt.getCreatedAt(),
                prompt.getUpdatedAt()
        );
    }

    public PromptSummaryResponse toPromptSummaryResponse(PromptEntity prompt, boolean isLiked) {
        UserEntity user = prompt.getUser();
        return new PromptSummaryResponse(
                prompt.getId(),
                user.getId(),
                user.getNickname(),
                toCategoryResponse(prompt.getCategory()),
                prompt.getTitle(),
                prompt.getContentSummary(),
                prompt.getCreatedAt(),
                prompt.getUpdatedAt(),
                isLiked
        );
    }

    public List<PromptSummaryResponse> toPromptSummaryResponseList(List<PromptEntity> prompts, Set<Long> likedPromptIds) {
        return prompts.stream()
                .map(p -> toPromptSummaryResponse(p, likedPromptIds.contains(p.getId())))
                .collect(Collectors.toList());
    }
}

