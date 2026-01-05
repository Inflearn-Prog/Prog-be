package com.progbe.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "user_social_links")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSocialLink {

    @Id
    @Column(name = "social_link_id")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity userEntity;

    @Column(nullable = false)
    private String provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    @Builder
    public UserSocialLink(UserEntity userEntity, String provider, String providerUserId) {
        this.id = UUID.randomUUID().toString();
        this.userEntity = userEntity;
        this.provider = provider;
        this.providerUserId = providerUserId;
    }
}