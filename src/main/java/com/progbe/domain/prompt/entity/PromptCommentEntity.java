package com.progbe.domain.prompt.entity;

import com.progbe.domain.prompt.dto.PromptCommentRequest;
import com.progbe.domain.prompt.type.PromptStatus;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment")
@Builder
@AllArgsConstructor
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

    @Column(name = "parent_id", nullable = true)
    private Long parentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'PUBLIC'")
    private PromptStatus promptStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "comment_status", nullable = false)
    private CommentStatus commentStatus;

    public PromptCommentEntity() {
    }

    public PromptCommentEntity(UserEntity user, PromptEntity prompt, String comment, Long parentId, CommentStatus commentStatus) {
        this.user = user;
        this.prompt = prompt;
        this.comment = comment;
        this.parentId = parentId;
        this.promptStatus = PromptStatus.PUBLIC;
        this.commentStatus = commentStatus;
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

    public Long getParentId() { return parentId; }

    public CommentStatus getCommentStatus() { return commentStatus; }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setCommentStatus(CommentStatus status) { this.commentStatus = status; }

    public void softDelete() {
        this.commentStatus = CommentStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    public static PromptCommentEntity createFrom(PromptEntity prompt, UserEntity user, PromptCommentRequest request) {
        return PromptCommentEntity.builder()
                .prompt(prompt)
                .user(user)
                .comment(request.comment())
                .commentStatus(CommentStatus.PUBLIC)
                .build();
    }

    public static PromptCommentEntity createFrom(PromptEntity prompt, UserEntity user, Long parentId, PromptCommentRequest request) {
        return PromptCommentEntity.builder()
                .prompt(prompt)
                .user(user)
                .comment(request.comment())
                .parentId(parentId)
                .commentStatus(CommentStatus.PUBLIC)
                .build();
    }

    public PromptStatus getPromptStatus() {
        return promptStatus;
    }

    public void updatePromptStatus(PromptStatus status) {
        this.promptStatus = status;
    }
}
