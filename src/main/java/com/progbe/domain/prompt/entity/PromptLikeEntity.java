package com.progbe.domain.prompt.entity;

import com.progbe.domain.user.entity.UserEntity;
import com.progbe.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
public class PromptLikeEntity extends BaseEntity {

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

    @Builder
    public PromptLikeEntity(UserEntity user, PromptEntity prompt) {
        this.user = user;
        this.prompt = prompt;
    }
}