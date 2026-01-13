package com.progbe.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "user_social_links",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_social_link_provider_id",
                        columnNames = {"provider", "provider_user_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSocialLinkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "social_link_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity userEntity;

    @Column(nullable = false)
    private String provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    @Column(name = "social_refresh_token", columnDefinition = "TEXT")
    private String socialRefreshToken;

    public void updateSocialRefreshToken(String socialRefreshToken) {
        this.socialRefreshToken = socialRefreshToken;
    }

    @Builder
    public UserSocialLinkEntity(UserEntity userEntity, String provider, String providerUserId, String socialRefreshToken) {
        this.userEntity = userEntity;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.socialRefreshToken = socialRefreshToken;
    }
}