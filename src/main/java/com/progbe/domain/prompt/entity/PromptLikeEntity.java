package com.progbe.domain.prompt.entity;

import com.progbe.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "prompt_likes",
        indexes = {
                @Index(name = "idx_prompt_likes_prompt_id", columnList = "prompt_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "pk_prompt_likes_user_prompt",
                        columnNames = {"user_id", "prompt_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PromptLikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id", nullable = false)
    private PromptEntity prompt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public PromptLikeEntity(UserEntity user, PromptEntity prompt) {
        this.user = user;
        this.prompt = prompt;
    }
}