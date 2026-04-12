package com.progbe.domain.prompt.entity;

import com.progbe.domain.user.entity.UserEntity;
import com.progbe.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@Builder
public class PromptLikeEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_like_prompt",
                    foreignKeyDefinition = "FOREIGN KEY (prompt_id) REFERENCES prompts(prompt_id) ON DELETE CASCADE"))
    private PromptEntity prompt;

    public PromptLikeEntity() { }

    public PromptLikeEntity(Long id, UserEntity user, PromptEntity prompt) {
        this.id = id;
        this.user = user;
        this.prompt = prompt;
    }

    public static PromptLikeEntity from(UserEntity user, PromptEntity prompt) {
        return PromptLikeEntity.builder()
                .user(user)
                .prompt(prompt)
                .build();
    }
}