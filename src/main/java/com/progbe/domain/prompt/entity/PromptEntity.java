package com.progbe.domain.prompt.entity;

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
                @Index(name = "idx_prompts_created_at", columnList = "created_at")
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

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false, length = 255)
    private String summary;

    @Column
    private String description;

    @Lob
    @Column(name = "example_input")
    private String exampleInput;

    @Lob
    @Column(name = "example_output")
    private String exampleOutput;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public PromptEntity(
            UserEntity user,
            CategoryEntity category,
            String title,
            String content,
            String summary,
            String description,
            String exampleInput,
            String exampleOutput
    ) {
        this.user = user;
        this.category = category;
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.description = description;
        this.exampleInput = exampleInput;
        this.exampleOutput = exampleOutput;
    }
}


