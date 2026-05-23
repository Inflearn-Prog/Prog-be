package com.progbe.domain.prompt.entity;

import com.progbe.domain.category.entity.CategoryEntity;
import com.progbe.domain.prompt.type.PromptStatus;
import com.progbe.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "prompts",
        indexes = {
                @Index(name = "idx_prompts_user_id", columnList = "user_id"),
                @Index(name = "idx_prompts_category_id", columnList = "category_id"),
                @Index(name = "idx_prompts_created_at", columnList = "created_at"),
                @Index(name = "idx_prompts_status", columnList = "status"),
                @Index(name = "idx_prompts_status_created_at", columnList = "status, created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PromptEntity {

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

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'PUBLIC'")
    private PromptStatus status = PromptStatus.PUBLIC;

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

    public void delete() {
        this.deletedAt = LocalDateTime.now();
        this.status = PromptStatus.DELETED;
    }

    public void updateStatus(PromptStatus status) {
        this.status = status;
        if (status == PromptStatus.DELETED && this.deletedAt == null) {
            this.deletedAt = LocalDateTime.now();
        }
    }

    public void updateCategory(CategoryEntity category) {
        this.category = category;
    }

    public String getContentSummary() {
        return content.length() > 70 ? content.substring(0, 70) + "..." : content;
    }
}
