package com.progbe.domain.prompt.entity;

import com.progbe.domain.category.entity.CategoryEntity;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "prompts",
        indexes = {
                @Index(name = "idx_prompts_user_id", columnList = "user_id"),
                @Index(name = "idx_prompts_category_id", columnList = "category_id"),
                @Index(name = "idx_prompts_created_at", columnList = "created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PromptEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prompt_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @OneToOne(mappedBy = "prompt", cascade = CascadeType.ALL, orphanRemoval = true)
    private PromptStatusEntity statusEntity;

    @Builder
    public PromptEntity(UserEntity user, CategoryEntity category, String title, String content) {
        this.user = user;
        this.category = category;
        this.title = title;
        this.content = content;
    }

    public void update(CategoryEntity category, String title, String content) {
        if (category != null) {
            this.category = category;
        }
        if (title != null) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
    }

    //프롬프트 생성 시 또는 status 테이블과 연결 시
    public void attachStatus(PromptStatusEntity statusEntity) {
        this.statusEntity = statusEntity;
    }
}

