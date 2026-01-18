package com.progbe.domain.prompt.entity;

import com.progbe.domain.category.entity.CategoryEntity;
import com.progbe.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "prompt_drafts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PromptDraftEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "draft_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @Column
    private String title;

    @Lob
    @Column
    private String content;

    @Column
    private String description;

    @Lob
    @Column(name = "tag_ids_json")
    private String tagIdsJson;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public PromptDraftEntity(
            UserEntity user,
            CategoryEntity category,
            String title,
            String content,
            String description,
            String tagIdsJson
    ) {
        this.user = user;
        this.category = category;
        this.title = title;
        this.content = content;
        this.description = description;
        this.tagIdsJson = tagIdsJson;
    }
}