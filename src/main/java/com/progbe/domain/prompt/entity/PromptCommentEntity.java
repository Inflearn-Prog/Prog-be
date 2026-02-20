package com.progbe.domain.prompt.entity;

import com.progbe.domain.prompt.type.PromptStatus;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.global.common.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "comment")
public class PromptCommentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id", nullable = false)
    private PromptEntity prompt;

    @Column(name = "comment", nullable = false)
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'PUBLIC'")
    private PromptStatus status;

    public PromptCommentEntity() {
    }

    public PromptCommentEntity(UserEntity user, PromptEntity prompt, String comment) {
        this.user = user;
        this.prompt = prompt;
        this.comment = comment;
        this.status = PromptStatus.PUBLIC;
    }

    public Long getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public PromptEntity getPrompt() {
        return prompt;
    }

    public String getComment() {
        return comment;
    }

    public PromptStatus getStatus() {
        return status;
    }

    public void updateStatus(PromptStatus status) {
        this.status = status;
    }
}
