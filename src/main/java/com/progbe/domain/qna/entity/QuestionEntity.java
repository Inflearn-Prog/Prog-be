package com.progbe.domain.qna.entity;

import com.progbe.domain.user.entity.UserEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "question")
public class QuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    public QuestionEntity() {}

    public QuestionEntity(UserEntity user, String title, String content) {
        this.user = user;
        this.title = title;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
