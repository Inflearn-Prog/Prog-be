package com.progbe.domain.prompt.entity;

import com.progbe.global.common.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "comment")
public class PromptCommentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long promptId;

    private String comment;

    public PromptCommentEntity() {}

    public PromptCommentEntity(Long userId, Long promptId, String comment) {
        this.userId = userId;
        this.promptId = promptId;
        this.comment = comment;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getPromptId() {
        return promptId;
    }

    public String getComment() {
        return comment;
    }
}
