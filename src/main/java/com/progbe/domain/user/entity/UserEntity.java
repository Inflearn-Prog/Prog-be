package com.progbe.domain.user.entity;

import com.progbe.domain.terms.entity.UserTermsAgreementEntity;
import com.progbe.domain.user.type.Role;
import com.progbe.domain.user.type.UserStatus;
import com.progbe.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column
    private String email;

    @Column(name = "profile_url")
    private String profileUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(name = "last_nickname_changed_at")
    private LocalDateTime lastNicknameChangedAt;

    @Column(name = "nickname_change_count", nullable = false)
    private Integer nicknameChangeCount = 0;

    @Column(name = "inactivated_at")
    private LocalDateTime inactivatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserSocialLinkEntity> socialLinks = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserTermsAgreementEntity> termsAgreements = new ArrayList<>();

    @Builder
    public UserEntity(String nickname, String email, String profileUrl, Role role, UserStatus status) {
        this.nickname = nickname;
        this.email = email;
        this.profileUrl = profileUrl;
        this.role = role;
        this.status = status;
        this.nicknameChangeCount = 0;
    }

    public void updateProfile(String nickname, String profileUrl) {
        if (nickname != null) this.nickname = nickname;
        if (profileUrl != null) this.profileUrl = profileUrl;
    }

    public void changeNicknameManually(String nickname, LocalDateTime changedAt) {
        this.nickname = nickname;
        this.lastNicknameChangedAt = changedAt;
        this.nicknameChangeCount++;
    }

    public void updateStatus(UserStatus status) {
        this.status = status;
    }

    @Override
    public void delete() {
        this.status = UserStatus.DELETED;
        super.delete();
    }
}