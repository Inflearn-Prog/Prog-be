package com.progbe.domain.qna.entity;

import com.progbe.domain.qna.dto.AnswerRequest;
import com.progbe.domain.user.entity.UserEntity;
import com.progbe.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "answer")
@Builder
@AllArgsConstructor
public class AnswerEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity admin;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private QuestionEntity question;

    @Column(name = "content", nullable = false)
    private String content;

    public AnswerEntity() {}

    public AnswerEntity(UserEntity admin, QuestionEntity question, String content) {
        this.admin = admin;
        this.question = question;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public UserEntity getAdmin() {
        return admin;
    }

    public QuestionEntity getQuestion() {
        return question;
    }

    public String getContent() {
        return content;
    }

    public static AnswerEntity from(UserEntity user, QuestionEntity question, AnswerRequest answerRequest) {
        return AnswerEntity.builder()
                .admin(user)
                .question(question)
                .content(answerRequest.content())
                .build();
    }

}
