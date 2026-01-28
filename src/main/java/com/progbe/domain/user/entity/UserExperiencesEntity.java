package com.progbe.domain.user.entity;

import com.progbe.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_experiences")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserExperiencesEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "experience_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "description", length = 200, nullable = false)
    private String description;

    @Builder
    public UserExperiencesEntity(UserEntity user, String description) {
        this.user = user;
        this.description = description;
    }
}