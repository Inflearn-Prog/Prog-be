package com.progbe.domain.prompt.entity;

import com.progbe.domain.prompt.type.PromptStatus;
import com.progbe.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "prompt_statuses",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_prompt_statuses_prompt_id", columnNames = {"prompt_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PromptStatusEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prompt_status_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id", nullable = false, unique = true)
    private PromptEntity prompt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PromptStatus status;

    @Builder
    public PromptStatusEntity(PromptEntity prompt, PromptStatus status) {
        this.prompt = prompt;
        this.status = status;
    }

    public void changeStatus(PromptStatus status) {
        this.status = status;
    }
}
