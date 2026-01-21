package com.progbe.domain.qna.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "answer")
public class AnswerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long adminId;

    private Long questionId;

    private String content;

    public AnswerEntity() {}

    public AnswerEntity(Long adminId, Long questionId, String content) {
        this.adminId = adminId;
        this.questionId = questionId;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public Long getAdminId() {
        return adminId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public String getContent() {
        return content;
    }
}
